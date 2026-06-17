package com.courseselect.controller;

import com.courseselect.client.DeepSeekClient;
import com.courseselect.client.DeepSeekClient.DeepSeekException;
import com.courseselect.config.DeepSeekConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 诊断接口 —— 检查 AI 配置和连通性
 */
@RestController
public class DebugController {

    private final DeepSeekConfig config;
    private final DeepSeekClient deepSeekClient;

    public DebugController(DeepSeekConfig config, DeepSeekClient deepSeekClient) {
        this.config = config;
        this.deepSeekClient = deepSeekClient;
    }

    /**
     * GET /api/debug/ai-config
     * 查看 AI 配置状态（不发起网络请求）
     */
    @GetMapping("/api/debug/ai-config")
    public Map<String, Object> debugConfig() {
        String key = config.getKey();
        boolean keySet = key != null && !key.isBlank() && !"your-api-key-here".equals(key);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baseUrl", config.getBaseUrl());
        result.put("model", config.getModel());
        result.put("keyLoaded", keySet);
        result.put("keyPreview", keySet
                ? (key.substring(0, Math.min(7, key.length())) + "...")
                : "NOT SET");
        result.put("maxTokens", config.getMaxTokens());
        result.put("temperature", config.getTemperature());
        return result;
    }

    /**
     * GET /api/debug/test-ai
     * 诊断 AI 连通性 —— 发送简单 ping，返回延迟和状态
     */
    @GetMapping("/api/debug/test-ai")
    public Map<String, Object> testAI() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. key 检查
        String key = config.getKey();
        boolean keySet = key != null && !key.isBlank() && !"your-api-key-here".equals(key);
        result.put("keyConfigured", keySet);

        if (!keySet) {
            result.put("status", "NOT_CONFIGURED");
            result.put("message", "API Key 未配置。请设置环境变量 DEEPSEEK_API_KEY。");
            return result;
        }

        // 2. 连通性测试
        Instant start = Instant.now();
        try {
            String reply = deepSeekClient.chat(
                    "你是一个测试助手。请只回复：OK",
                    "请回复 OK");
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();

            result.put("status", "OK");
            result.put("latencyMs", elapsedMs);
            result.put("reply", reply.trim());
        } catch (DeepSeekException e) {
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();
            result.put("status", "ERROR");
            result.put("latencyMs", elapsedMs);
            result.put("error", e.getMessage());
        } catch (Exception e) {
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();
            result.put("status", "ERROR");
            result.put("latencyMs", elapsedMs);
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return result;
    }
}