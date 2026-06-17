package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.entity.Student;
import com.courseselect.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生查询工具 —— 返回学生画像（专业、年级、GPA、兴趣、已修学分）。
 */
@Component
public class QueryStudentTool implements Tool {

    private final StudentService studentService;
    private final ObjectMapper objectMapper;

    public QueryStudentTool(StudentService studentService, ObjectMapper objectMapper) {
        this.studentService = studentService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "query_student";
    }

    @Override
    public String getDescription() {
        return "查询学生信息。返回学号、姓名、专业、年级、GPA、兴趣方向、已修学分。每次对话开始时建议先调用以获取最新状态。";
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

        schema.put("properties", props);
        schema.put("required", List.of("studentNo"));
        return schema;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String studentNo = (String) params.get("studentNo");
        try {
            Student s = studentService.findByStudentNo(studentNo);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("studentNo", s.getStudentNo());
            info.put("name", s.getName());
            info.put("major", s.getMajor());
            info.put("grade", s.getGrade());
            info.put("gpa", s.getGpa());
            info.put("interests", s.getInterests());
            info.put("completedCredits", s.getCompletedCredits());
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
