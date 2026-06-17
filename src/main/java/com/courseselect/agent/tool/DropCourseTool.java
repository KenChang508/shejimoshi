package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.service.SelectionService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 退选工具 —— 调用后端 SelectionService 退选课程。
 */
@Component
public class DropCourseTool implements Tool {

    private final SelectionService selectionService;

    public DropCourseTool(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @Override
    public String getName() {
        return "drop_course";
    }

    @Override
    public String getDescription() {
        return "退选课程。需要学生学号和课程代码。只能退选当前已选中的课程（状态为 SELECTED）。";
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
            selectionService.dropCourse(studentNo, courseCode);
            return String.format("{\"success\":true,\"message\":\"课程 %s 退选成功\"}", courseCode);
        } catch (Exception e) {
            return String.format("{\"success\":false,\"error\":\"%s\"}", e.getMessage().replace("\"", "'"));
        }
    }
}
