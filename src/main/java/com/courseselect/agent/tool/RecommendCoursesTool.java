package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.dto.RecommendResponse;
import com.courseselect.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 课程推荐工具 —— 调用推荐引擎为指定学生推荐课程。
 */
@Component
public class RecommendCoursesTool implements Tool {

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public RecommendCoursesTool(RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "recommend_courses";
    }

    @Override
    public String getDescription() {
        return "AI 课程推荐。根据学生专业、兴趣、已修课程和当前可选课程，生成个性化推荐列表（含匹配度评分和推荐理由）。可用于学生在不确定选什么课时获取参考。";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();

        Map<String, Object> studentNo = new LinkedHashMap<>();
        studentNo.put("type", "string");
        studentNo.put("description", "学生学号");
        props.put("studentNo", studentNo);

        Map<String, Object> semester = new LinkedHashMap<>();
        semester.put("type", "string");
        semester.put("description", "目标学期，如 2025-2026-2");
        props.put("semester", semester);

        Map<String, Object> limit = new LinkedHashMap<>();
        limit.put("type", "integer");
        limit.put("description", "推荐数量，默认 5，最大 10");
        props.put("limit", limit);

        schema.put("properties", props);
        schema.put("required", List.of("studentNo", "semester"));
        return schema;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String studentNo = (String) params.get("studentNo");
        String semester = (String) params.get("semester");
        int limit = params.containsKey("limit") ? ((Number) params.get("limit")).intValue() : 5;
        try {
            RecommendResponse response = recommendationService.recommend(studentNo, semester, Math.min(limit, 10));
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
