package com.courseselect.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学号，唯一 */
    @Column(nullable = false, unique = true, length = 20)
    private String studentNo;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 专业 */
    @Column(nullable = false, length = 50)
    private String major;

    /** 年级（入学年份） */
    @Column(nullable = false)
    private Integer grade;

    /** 绩点（0.00 - 4.00） */
    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    /** 兴趣方向，逗号分隔 */
    @Column(length = 500)
    private String interests;

    /** 已修学分 */
    @Column(nullable = false)
    @Builder.Default
    private Integer completedCredits = 0;
}
