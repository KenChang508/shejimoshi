package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.service.SelectionService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 选课工具 —— 调用后端 SelectionService 执行选课。
 */
@Component
public class SelectCourseTool implements Tool {

    private final SelectionService selectionService;

    public SelectCourseTool(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @Override
    public String getName() {
        return "select_course";
    }

    @Override
    public String getDescription() {
        return "为学生选课。需要学生学号和课程代码。选课前应先确认课程有余量且学生满足先修课要求，必要时逐门选课。";
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

        Map<String, Object> courseCode = new LinkedHashMap<>();
        courseCode.put("type", "string");
        courseCode.put("description", "课程代码");
        props.put("courseCode", courseCode);

        schema.put("properties", props);
        schema.put("required", List.of("studentNo", "courseCode"));
        return schema;
    }

    @Override
    public String execute(Map<String, Object> params) {
        String studentNo = (String) params.get("studentNo");
        String courseCode = (String) params.get("courseCode");
        try {
            var selection = selectionService.selectCourse(studentNo, courseCode);
            return String.format(
                    "{\"success\":true,\"message\":\"课程 %s 选课成功\",\"courseName\":\"%s\"}",
                    courseCode,
                    selection.getCourseName() != null ? selection.getCourseName() : courseCode);
        } catch (Exception e) {
            return String.format("{\"success\":false,\"error\":\"%s\"}", e.getMessage().replace("\"", "'"));
        }
    }
}
