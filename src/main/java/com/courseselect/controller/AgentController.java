package com.courseselect.controller;

import com.courseselect.agent.AgentLoop;
import com.courseselect.agent.AgentLoop.AgentResult;
import com.courseselect.agent.AgentLoop.ToolCallLog;
import com.courseselect.dto.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体对话接口 —— POST /api/agent/chat
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentLoop agentLoop;

    public AgentController(AgentLoop agentLoop) {
        this.agentLoop = agentLoop;
    }

    /**
     * 发送消息给智能体，返回回复及工具调用日志。
     */
    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        AgentResult result = agentLoop.run(
                request.getStudentNo(),
                request.getMessage(),
                request.getSessionId());

        List<ToolCallVO> toolVOs = result.toolCalls().stream()
                .map(tc -> new ToolCallVO(tc.toolName(), tc.success(), tc.summary()))
                .toList();

        AgentChatResponse response = AgentChatResponse.builder()
                .sessionId(result.sessionId())
                .reply(result.reply())
                .toolCalls(toolVOs)
                .elapsedMs(result.elapsedMs())
                .build();

        return ApiResponse.ok(response);
    }

    // ================================================================
    // DTO
    // ================================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentChatRequest {
        @NotBlank(message = "学号不能为空")
        private String studentNo;

        @NotBlank(message = "消息不能为空")
        private String message;

        /** 会话 ID。首次不传，后续传入以维持上下文 */
        private String sessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentChatResponse {
        private String sessionId;
        private String reply;
        private List<ToolCallVO> toolCalls;
        private long elapsedMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallVO {
        private String toolName;
        private boolean success;
        private String summary;
    }
}
