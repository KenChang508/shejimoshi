package com.courseselect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "selections",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "course_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Selection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 选课学生 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** 所选课程 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** 选课时间 */
    @Column(nullable = false)
    private LocalDateTime selectionTime;

    /** 选课状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SelectionStatus status;

    // ---------------------------------------------------------------
    // 选课状态枚举
    // ---------------------------------------------------------------
    public enum SelectionStatus {
        /** 已选（在修） */
        SELECTED,
        /** 已退选 */
        DROPPED,
        /** 已完成（通过） */
        COMPLETED
    }
}
