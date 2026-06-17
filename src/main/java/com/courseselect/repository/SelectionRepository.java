package com.courseselect.repository;

import com.courseselect.entity.Selection;
import com.courseselect.entity.Selection.SelectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SelectionRepository extends JpaRepository<Selection, Long> {

    /** 查询某学生某课程的唯一选课记录 */
    Optional<Selection> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /** 某学生的所有选课记录 */
    List<Selection> findByStudentId(Long studentId);

    /** 某学生指定状态的选课 */
    List<Selection> findByStudentIdAndStatus(Long studentId, SelectionStatus status);

    /** 已完成课程的 courseCode 列表（用于先修课判断） */
    @Query("SELECT s.course.courseCode FROM Selection s " +
           "WHERE s.student.id = :studentId AND s.status = 'COMPLETED'")
    List<String> findCompletedCourseCodes(@Param("studentId") Long studentId);

    /** 某课程已选人数 */
    long countByCourseIdAndStatus(Long courseId, SelectionStatus status);
}
