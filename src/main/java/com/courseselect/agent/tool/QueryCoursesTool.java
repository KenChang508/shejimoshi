package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.dto.CourseDTO;
import com.courseselect.entity.Course;
import com.courseselect.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 课程查询工具 —— 可按学期、类别、关键词筛选。
 */
@Component
public class QueryCoursesTool implements Tool {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    public QueryCoursesTool(CourseService courseService, ObjectMapper objectMapper) {
        this.courseService = courseService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "query_courses";
    }

    @Override
    public String getDescription() {
        return "查询可选课程列表。可按学期、类别、关键词筛选。返回课程代码、名称、学分、类别、课容量/已选、上课时间地点、先修课要求、简介、授课教师。";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();

        Map<String, Object> semester = new LinkedHashMap<>();
        semester.put("type", "string");
        semester.put("description", "学期，如 2025-2026-2。不填则查询全部学期");
        props.put("semester", semester);

        Map<String, Object> category = new LinkedHashMap<>();
        category.put("type", "string");
        category.put("description", "类别：必修 或 选修。不填则查询全部");
        props.put("category", category);

        Map<String, Object> keyword = new LinkedHashMap<>();
        keyword.put("type", "string");
        keyword.put("description", "搜索关键词，匹配课程名称、描述或代码");
        props.put("keyword", keyword);

        schema.put("properties", props);
        schema.put("required", List.of());
        return schema;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String semester = (String) params.get("semester");
        String category = (String) params.get("category");
        String keyword = (String) params.get("keyword");

        List<Course> courses;
        if (semester != null && !semester.isBlank() && category != null && !category.isBlank()) {
            courses = courseService.findBySemesterAndCategory(semester, category);
        } else if (semester != null && !semester.isBlank()) {
            courses = courseService.findBySemester(semester);
        } else {
            courses = courseService.findAll();
        }

        // 关键词过滤
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            courses = courses.stream()
                    .filter(c -> matches(c, kw))
                    .toList();
        }

        List<CourseDTO> dtos = courses.stream().map(CourseService::toDTO).toList();
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "count", dtos.size(),
                    "courses", dtos));
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }

    private boolean matches(Course c, String kw) {
        return (c.getName() != null && c.getName().toLowerCase().contains(kw))
                || (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw))
                || (c.getCourseCode() != null && c.getCourseCode().toLowerCase().contains(kw));
    }
}
