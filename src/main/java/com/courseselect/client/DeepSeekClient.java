package com.courseselect.client;

import com.courseselect.config.DeepSeekConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek API 客户端 —— 支持纯文本聊天与 Function Calling。
 * 兼容 DeepSeek V3/V4，处理 reasoning_content，提供清晰的错误诊断。
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final DeepSeekConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String PLACEHOLDER_KEY = "your-api-key-here";

    public DeepSeekClient(DeepSeekConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // ================================================================
    // 原有纯文本聊天方法（保持兼容）
    // ================================================================

    public String chat(String systemPrompt, String userMessage) {
        String key = config.getKey();
        if (key == null || key.isBlank() || PLACEHOLDER_KEY.equals(key)) {
            throw new DeepSeekException(
                    "DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY，"
                            + "或在 application.yml 中修改 deepseek.api.key 的值。");
        }
        if (!key.startsWith("sk-")) {
            log.warn("API Key 格式可能有误（通常以 'sk-' 开头），仍将尝试调用");
        }
        if (userMessage.length() > 16000) {
            log.warn("用户消息较长 ({} 字符)，可能超出模型 token 限制", userMessage.length());
        }

        try {
            return doChat(systemPrompt, userMessage, false);
        } catch (DeepSeekException e) {
            if (e.getMessage() != null && e.getMessage().contains("json_object")) {
                log.warn("JSON 模式失败，降级为纯文本模式重试");
                return doChat(systemPrompt, userMessage, true);
            }
            throw e;
        }
    }

    private String doChat(String systemPrompt, String userMessage, boolean plainText) {
        try {
            ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                    .model(config.getModel())
                    .messages(List.of(
                            new Message("system", systemPrompt),
                            new Message("user", userMessage)
                    ))
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature());

            if (!plainText) {
                builder.responseFormat(new ResponseFormat("json_object"));
            }

            ChatRequest request = builder.build();
            String body = objectMapper.writeValueAsString(request);

            log.info("DeepSeek API 调用中... (prompt {} 字符, model={})",
                    body.length(), config.getModel());

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errBody = response.body();
                log.error("DeepSeek API 返回 {}: {}",
                        response.statusCode(),
                        errBody.length() > 300 ? errBody.substring(0, 300) + "..." : errBody);
                throw new DeepSeekException(
                        extractErrorMessage(response.statusCode(), errBody));
            }

            ChatResponse chatResponse = objectMapper.readValue(response.body(), ChatResponse.class);
            String content = extractContent(chatResponse);
            log.info("DeepSeek 响应长度: {} 字符", content.length());
            return content;

        } catch (DeepSeekException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek API 调用异常", e);
            throw new DeepSeekException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    // ================================================================
    // Function Calling 方法（智能体专用）
    // ================================================================

    /**
     * 发送 Function Calling 请求，返回结构化结果。
     * 若模型决定调用工具，response.toolCalls 非空；否则取 response.content。
     */
    public FunctionChatResponse functionChat(List<Message> messages,
                                              List<Map<String, Object>> tools) {
        String key = config.getKey();
        if (key == null || key.isBlank() || PLACEHOLDER_KEY.equals(key)) {
            throw new DeepSeekException(
                    "DeepSeek API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY。");
        }

        try {
            ChatRequest request = ChatRequest.builder()
                    .model(config.getModel())
                    .messages(messages)
                    .tools(tools)
                    .toolChoice("auto")
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature())
                    .build();

            String body = objectMapper.writeValueAsString(request);

            log.debug("Function Calling 请求: {} 条消息, {} 个工具, {} 字符",
                    messages.size(), tools != null ? tools.size() : 0, body.length());

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errBody = response.body();
                log.error("Function Calling API 返回 {}: {}",
                        response.statusCode(),
                        errBody.length() > 300 ? errBody.substring(0, 300) + "..." : errBody);
                throw new DeepSeekException(
                        extractErrorMessage(response.statusCode(), errBody));
            }

            ChatResponse chatResponse = objectMapper.readValue(response.body(), ChatResponse.class);

            if (chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                throw new DeepSeekException("AI 未返回有效结果");
            }

            Message msg = chatResponse.getChoices().get(0).getMessage();
            if (msg == null) {
                throw new DeepSeekException("AI 响应中 message 为空");
            }

            FunctionChatResponse result = new FunctionChatResponse();
            result.setContent(msg.getContent());
            result.setToolCalls(msg.getToolCalls());
            result.setFinishReason(
                    chatResponse.getChoices().get(0).getFinishReason());

            log.debug("Function Calling 响应: content={}, tool_calls={}",
                    result.getContent() != null ? result.getContent().length() + "字符" : "null",
                    result.getToolCalls() != null ? result.getToolCalls().size() : 0);

            return result;

        } catch (DeepSeekException e) {
            throw e;
        } catch (Exception e) {
            log.error("Function Calling 异常", e);
            throw new DeepSeekException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    // ================================================================
    // 工具方法
    // ================================================================

    private String extractContent(ChatResponse chatResponse) {
        Message msg = chatResponse.getChoices().get(0).getMessage();
        String content = msg.getContent();
        if (content == null || content.isBlank()) {
            String reasoning = msg.getReasoningContent();
            if (reasoning != null && !reasoning.isBlank()) {
                log.warn("AI 只返回了 reasoning_content ({} 字符)，无最终 content。"
                                + "请确认 model 为 'deepseek-chat' 而非 'deepseek-reasoner'。",
                        reasoning.length());
            }
            throw new DeepSeekException(
                    "AI 返回内容为空。如果使用了 deepseek-reasoner 模型，请切换为 deepseek-chat。");
        }
        return content;
    }

    private String extractErrorMessage(int statusCode, String body) {
        String apiMessage = extractError(body);
        return switch (statusCode) {
            case 401 -> "API Key 无效或未授权。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            case 402 -> "账户余额不足，请充值。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            case 403 -> "API 访问被拒绝。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            case 429 -> "请求过于频繁（429 限流），请稍后重试。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            case 500 -> "DeepSeek 服务内部错误。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            case 502, 503, 504 -> "DeepSeek 服务暂时不可用 (HTTP " + statusCode + ")。"
                    + (apiMessage.isEmpty() ? "" : " 详情: " + apiMessage);
            default -> "AI 服务调用失败 (HTTP " + statusCode + ")"
                    + (apiMessage.isEmpty() ? "" : ": " + apiMessage);
        };
    }

    private String extractError(String body) {
        try {
            var node = objectMapper.readTree(body);
            if (node.has("error")) {
                var err = node.get("error");
                if (err.has("message")) return err.get("message").asText();
                if (err.has("code")) return "code=" + err.get("code").asText();
            }
        } catch (Exception ignored) {}
        return "";
    }

    // ================================================================
    // 内部模型类 —— 请求
    // ================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatRequest {
        private String model;
        private List<Message> messages;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private Double temperature;

        @JsonProperty("response_format")
        private ResponseFormat responseFormat;

        /** Function Calling 工具定义 */
        private List<Map<String, Object>> tools;

        /** 工具选择策略：auto / none / required */
        @JsonProperty("tool_choice")
        private Object toolChoice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;

        /** AI 最终回复内容（tool_calls 存在时可能为 null） */
        private String content;

        /** DeepSeek V3/V4 思维链内容 */
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        /** Function Calling 工具调用列表（assistant 消息中） */
        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;

        /** tool 消息的 tool_call_id，关联到具体调用 */
        @JsonProperty("tool_call_id")
        private String toolCallId;

        // ---------- 便捷构造器 ----------

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        /** 创建一条 tool 角色消息（工具执行结果） */
        public static Message toolResult(String callId, String result) {
            Message m = new Message();
            m.role = "tool";
            m.toolCallId = callId;
            m.content = result;
            return m;
        }

        /** 创建一条 assistant 角色消息（含 tool_calls） */
        public static Message withToolCalls(List<ToolCall> toolCalls) {
            Message m = new Message();
            m.role = "assistant";
            m.toolCalls = toolCalls;
            return m;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseFormat {
        private String type;
    }

    // ================================================================
    // ToolCall —— Function Calling 工具调用
    // ================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolCall {
        private String id;
        private String type;

        @JsonProperty("function")
        private FunctionCall function;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FunctionCall {
            private String name;
            private String arguments;
        }
    }

    // ================================================================
    // FunctionChatResponse —— Function Calling 结果
    // ================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionChatResponse {
        /** 模型最终文本回复（无 tool_calls 时） */
        private String content;

        /** 模型决定调用的工具列表 */
        private List<ToolCall> toolCalls;

        /** API 返回的 finish_reason（stop / tool_calls / length） */
        private String finishReason;

        public boolean hasToolCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }

    // ================================================================
    // 内部模型类 —— 响应
    // ================================================================

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatResponse {
        private List<Choice> choices;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            private Message message;

            @JsonProperty("finish_reason")
            private String finishReason;
        }
    }

    // ================================================================
    // 专用异常
    // ================================================================

    public static class DeepSeekException extends RuntimeException {
        public DeepSeekException(String message) {
            super(message);
        }
        public DeepSeekException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
