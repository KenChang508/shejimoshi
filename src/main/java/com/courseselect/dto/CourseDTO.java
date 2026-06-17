package com.courseselect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程信息 DTO（用于 API 返回，含教师信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

    private Long id;
    private String courseCode;
    private String name;
    private String teacherName;
    private String teacherTitle;
    private Integer credits;
    private Integer capacity;
    private Integer enrolled;
    private String schedule;
    private String location;
    private String category;
    private String prerequisites;
    private String description;
    private String semester;

    /** 是否仍有名额 */
    public boolean hasCapacity() {
        return enrolled < capacity;
    }

    /** 剩余名额 */
    public int remaining() {
        return capacity - enrolled;
    }
}
