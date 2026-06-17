package com.courseselect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 课程代码，唯一 */
    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    /** 课程名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 授课教师 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    /** 学分 */
    @Column(nullable = false)
    private Integer credits;

    /** 课容量上限 */
    @Column(nullable = false)
    private Integer capacity;

    /** 已选人数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer enrolled = 0;

    /** 上课时间，如 "周一 1-2节" / "周三 3-4节" */
    @Column(length = 50)
    private String schedule;

    /** 上课地点 */
    @Column(length = 50)
    private String location;

    /** 课程类别：必修 / 选修 */
    @Column(length = 20)
    private String category;

    /** 先修课程代码，逗号分隔 */
    @Column(length = 500)
    private String prerequisites;

    /** 课程简介 */
    @Column(length = 1000)
    private String description;

    /** 开课学期，如 "2025-2026-2" */
    @Column(length = 20)
    private String semester;
}
