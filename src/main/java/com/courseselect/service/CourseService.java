package com.courseselect.service;

import com.courseselect.dto.CourseDTO;
import com.courseselect.entity.Course;
import com.courseselect.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /** 查询所有课程 */
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    /** 按学期查询 */
    @Transactional(readOnly = true)
    public List<Course> findBySemester(String semester) {
        return courseRepository.findBySemester(semester);
    }

    /** 按学期和类别查询 */
    @Transactional(readOnly = true)
    public List<Course> findBySemesterAndCategory(String semester, String category) {
        return courseRepository.findBySemesterAndCategory(semester, category);
    }

    /** 查询尚有余额的课程 */
    @Transactional(readOnly = true)
    public List<Course> findAvailable(String semester) {
        return courseRepository.findAvailableBySemester(semester);
    }

    /** 按 ID 查询 */
    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("课程不存在: " + id));
    }

    /** 按课程代码查询 */
    @Transactional(readOnly = true)
    public Course findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new EntityNotFoundException("课程不存在: " + courseCode));
    }

    /** 实体转 DTO */
    public static CourseDTO toDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .teacherTitle(course.getTeacher() != null ? course.getTeacher().getTitle() : null)
                .credits(course.getCredits())
                .capacity(course.getCapacity())
                .enrolled(course.getEnrolled())
                .schedule(course.getSchedule())
                .location(course.getLocation())
                .category(course.getCategory())
                .prerequisites(course.getPrerequisites())
                .description(course.getDescription())
                .semester(course.getSemester())
                .build();
    }
}
