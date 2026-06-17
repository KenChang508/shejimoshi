package com.courseselect.agent.tool;

import com.courseselect.agent.Tool;
import com.courseselect.dto.SelectionDTO;
import com.courseselect.entity.Selection;
import com.courseselect.entity.Student;
import com.courseselect.service.SelectionService;
import com.courseselect.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学分检查工具 —— 汇总学生已完成学分 + 当前学期已选学分 + 余量。
 */
@Component
public class CheckCreditsTool implements Tool {

    private final StudentService studentService;
    private final SelectionService selectionService;
    private final ObjectMapper objectMapper;

    public CheckCreditsTool(StudentService studentService,
                            SelectionService selectionService,
                            ObjectMapper objectMapper) {
        this.studentService = studentService;
        this.selectionService = selectionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "check_credits";
    }

    @Override
    public String getDescription() {
        return "检查学生学分状态。返回已修总学分、当前学期已选学分、建议上限（25）、剩余可选学分、当前已选课程列表。选课前宜先调用以确认学分余量。";
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
            Student student = studentService.findByStudentNo(studentNo);
            List<SelectionDTO> selections = selectionService.findByStudent(studentNo);

            int currentCredits = selections.stream()
                    .filter(s -> s.getStatus() == Selection.SelectionStatus.SELECTED)
                    .mapToInt(s -> s.getCredits())
                    .sum();

            int maxPerSemester = 25;

            List<String> currentCourses = selections.stream()
                    .filter(s -> s.getStatus() == Selection.SelectionStatus.SELECTED)
                    .map(s -> s.getCourseCode() + " " + s.getCourseName()
                            + "(" + s.getCredits() + "学分)")
                    .toList();

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("completedCredits", student.getCompletedCredits());
            info.put("currentSemesterCredits", currentCredits);
            info.put("maxPerSemester", maxPerSemester);
            info.put("remainingCredits", Math.max(0, maxPerSemester - currentCredits));
            info.put("currentCourses", currentCourses);

            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
