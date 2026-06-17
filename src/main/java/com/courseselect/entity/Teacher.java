package com.courseselect.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 工号，唯一 */
    @Column(nullable = false, unique = true, length = 20)
    private String teacherNo;

    /** 姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 所属院系 */
    @Column(length = 50)
    private String department;

    /** 职称 */
    @Column(length = 20)
    private String title;
}
