package com.courseselect.agent;

import com.courseselect.client.DeepSeekClient;
import com.courseselect.client.DeepSeekClient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ReAct 智能体循环引擎。
 *
 * 核心流程：
 *   User Message → 拼 messages → functionChat → 
 *   有 tool_calls → 执行工具 → 结果塞回 messages → 循环
 *   无 tool_calls → 返回最终回答
 *
 * 最多循环 MAX_ITERATIONS 轮，超限强制让模型汇总。
 */
@Service
public class AgentLoop {

    private static final Logger log = LoggerFactory.getLogger(AgentLoop.class);
    private static final int MAX_ITERATIONS = 10;

    private final DeepSeekClient deepSeekClient;
    private final ToolRegistry toolRegistry;

    /** 会话存储：sessionId → 历史消息（只存 user/assistant，不含中间 tool 消息） */
    private final Map<String, List<Message>> sessionStore = new ConcurrentHashMap<>();

    public AgentLoop(DeepSeekClient deepSeekClient, ToolRegistry toolRegistry) {
        this.deepSeekClient = deepSeekClient;
        this.toolRegistry = toolRegistry;
    }

    /**
     * 执行一次智能体对话。
     *
     * @param studentNo  当前学生学号
     * @param userMessage 用户发送的消息
     * @param sessionId  会话 ID（null 则创建新会话）
     * @return 智能体的最终回复
     */
    public AgentResult run(String studentNo, String userMessage, String sessionId) {
        long startTime = System.currentTimeMillis();

        // ── 会话管理 ──
        String sid = (sessionId != null && !sessionId.isBlank())
                ? sessionId : UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        List<Message> history = sessionStore.computeIfAbsent(sid, k -> new ArrayList<>());

        // ── 构建 messages ──
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", buildSystemPrompt(studentNo)));
        messages.addAll(history);
        messages.add(new Message("user", userMessage));

        // ── 工具调用记录 ──
        List<ToolCallLog> toolLogs = new ArrayList<>();

        // ── ReAct 循环 ──
        try {
            for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
                log.debug("Agent 轮次 {}/{}", iter + 1, MAX_ITERATIONS);

                FunctionChatResponse response;
                try {
                    response = deepSeekClient.functionChat(
                            messages, toolRegistry.getFunctionDefinitions());
                } catch (DeepSeekException e) {
                    log.error("Agent 调用失败 (轮次 {}): {}", iter + 1, e.getMessage());
                    String fallback = "抱歉，AI 服务暂时不可用：" + e.getMessage();
                    return new AgentResult(sid, fallback, toolLogs,
                            System.currentTimeMillis() - startTime);
                }

                // ── 模型决定调用工具 ──
                if (response.hasToolCalls()) {
                    List<ToolCall> toolCalls = response.getToolCalls();

                    // 添加 assistant 消息（含 tool_calls）
                    messages.add(Message.withToolCalls(toolCalls));

                    // 逐个执行工具
                    for (ToolCall tc : toolCalls) {
                        String funcName = tc.getFunction() != null
                                ? tc.getFunction().getName() : "unknown";
                        String args = tc.getFunction() != null
                                ? tc.getFunction().getArguments() : "{}";

                        log.info("Agent 调用工具: {} (args={})", funcName,
                                args.length() > 100 ? args.substring(0, 100) + "..." : args);

                        String toolResult = toolRegistry.execute(funcName, args);
                        messages.add(Message.toolResult(tc.getId(), toolResult));

                        toolLogs.add(new ToolCallLog(funcName,
                                !toolResult.contains("\"error\""),
                                summarize(funcName, toolResult)));
                    }

                    // 如果 finish_reason 为 stop，不再循环
                    if ("stop".equals(response.getFinishReason())) {
                        break;
                    }
                    continue;
                }

                // ── 模型返回最终文本 ──
                String content = response.getContent();
                if (content == null || content.isBlank()) {
                    content = "抱歉，我没有理解你的需求，请换种方式描述。";
                }

                // 存入会话历史
                history.add(new Message("user", userMessage));
                history.add(new Message("assistant", content));

                return new AgentResult(sid, content, toolLogs,
                        System.currentTimeMillis() - startTime);
            }

            // ── 超出最大轮次，强制汇总 ──
            messages.add(new Message("user", "请根据之前获取的所有信息，给出最终答案。不要再调用工具了。"));
            FunctionChatResponse finalResponse = deepSeekClient.functionChat(
                    messages, toolRegistry.getFunctionDefinitions());
            String finalContent = finalResponse.getContent() != null
                    ? finalResponse.getContent() : "处理超时，请简化问题后重试。";

            history.add(new Message("user", userMessage));
            history.add(new Message("assistant", finalContent));

            return new AgentResult(sid, finalContent, toolLogs,
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("Agent 循环异常", e);
            return new AgentResult(sid,
                    "处理过程中发生错误：" + e.getMessage(),
                    toolLogs, System.currentTimeMillis() - startTime);
        }
    }

    // ================================================================
    // 系统提示词
    // ================================================================

    private String buildSystemPrompt(String studentNo) {
        return """
                你是一个大学智能选课助手，运行在选课系统中。你可以调用工具来帮助学生完成选课任务。

                ## 可用工具
                - query_student: 查询学生信息（专业、年级、GPA、兴趣、已修学分）
                - query_courses: 查询可选课程（可按学期、类别、关键词筛选）
                - check_credits: 检查学分状态（已修/当前/余量）
                - recommend_courses: AI 课程推荐（根据学生画像智能匹配）
                - select_course: 执行选课
                - drop_course: 退选课程

                ## 工作原则
                1. 先理解用户意图，再选择工具——不要盲目调用
                2. 选课前必须确认：课程有余量、先修课已满足、学分不超限
                3. 如需选多门课，逐门检查条件后再执行
                4. 操作失败时解释原因并给出替代建议
                5. 单次对话最多选 5 门课
                6. 回复简洁友好，使用中文
                7. 学生要求看课程/学分等信息时，直接用工具获取，不要猜测

                ## 当前学生
                学号: %s
                如需了解学生详情，请使用 query_student 工具。
                """.formatted(studentNo);
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    /** 提取工具结果的简短摘要（用于前端展示） */
    private String summarize(String toolName, String result) {
        if (result.contains("\"error\"")) {
            return "执行失败";
        }
        return switch (toolName) {
            case "query_student" -> "已获取学生信息";
            case "query_courses" -> {
                int idx = result.indexOf("\"count\":");
                if (idx >= 0) {
                    String sub = result.substring(idx + 8);
                    int end = sub.indexOf(",");
                    if (end < 0) end = sub.indexOf("}");
                    yield "查询到 " + (end > 0 ? sub.substring(0, end) : "?") + " 门课程";
                }
                yield "已查询课程列表";
            }
            case "check_credits" -> "已查询学分状态";
            case "recommend_courses" -> "已生成课程推荐";
            case "select_course" -> result.contains("\"success\":true") ? "选课成功" : "选课失败";
            case "drop_course" -> result.contains("\"success\":true") ? "退选成功" : "退选失败";
            default -> "已完成";
        };
    }

    // ================================================================
    // 内部数据类
    // ================================================================

    /** 智能体执行结果 */
    public record AgentResult(String sessionId, String reply,
                               List<ToolCallLog> toolCalls, long elapsedMs) {}

    /** 单次工具调用日志 */
    public record ToolCallLog(String toolName, boolean success, String summary) {}
}
