package com.courseselect.dto;

import com.courseselect.entity.Selection.SelectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课记录 DTO（展平 Selection + Student + Course + Teacher 信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectionDTO {

    // ---- 选课记录 ----
    private Long selectionId;
    private LocalDateTime selectionTime;
    private SelectionStatus status;

    // ---- 学生 ----
    private Long studentId;
    private String studentNo;
    private String studentName;

    // ---- 课程 ----
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private Integer credits;
    private String schedule;
    private String location;
    private String category;
    private String semester;
}
