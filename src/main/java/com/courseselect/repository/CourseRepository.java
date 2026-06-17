package com.courseselect.repository;

import com.courseselect.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    /** 按学期查询可选课程 */
    List<Course> findBySemester(String semester);

    /** 按学期和类别查询 */
    List<Course> findBySemesterAndCategory(String semester, String category);

    /** 查询仍有名额的课程 */
    @Query("SELECT c FROM Course c WHERE c.semester = :semester AND c.enrolled < c.capacity")
    List<Course> findAvailableBySemester(@Param("semester") String semester);
}
