package com.courseselect.service;

import com.courseselect.dto.SelectionDTO;
import com.courseselect.entity.Course;
import com.courseselect.entity.Selection;
import com.courseselect.entity.Selection.SelectionStatus;
import com.courseselect.entity.Student;
import com.courseselect.repository.SelectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SelectionService {

    private final SelectionRepository selectionRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    public SelectionService(SelectionRepository selectionRepository,
                            StudentService studentService,
                            CourseService courseService) {
        this.selectionRepository = selectionRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /**
     * 学生选课
     *
     * @param studentNo  学号
     * @param courseCode 课程代码
     * @return 选课记录
     */
    public SelectionDTO selectCourse(String studentNo, String courseCode) {
        Student student = studentService.findByStudentNo(studentNo);
        Course course = courseService.findByCourseCode(courseCode);

        // 检查是否已选过
        selectionRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
                .ifPresent(s -> {
                    if (s.getStatus() == SelectionStatus.SELECTED) {
                        throw new IllegalStateException("已选过该课程");
                    }
                });

        // 检查课容量
        if (course.getEnrolled() >= course.getCapacity()) {
            throw new IllegalStateException("课程已满");
        }

        // 检查先修课
        if (course.getPrerequisites() != null && !course.getPrerequisites().isBlank()) {
            List<String> completed = selectionRepository.findCompletedCourseCodes(student.getId());
            for (String prereq : course.getPrerequisites().split(",")) {
                prereq = prereq.trim();
                if (!completed.contains(prereq)) {
                    throw new IllegalStateException(
                            "未满足先修课要求: " + prereq + "（课程 " + course.getName() + " 需要先修）");
                }
            }
        }

        // 创建选课记录
        Selection selection = Selection.builder()
                .student(student)
                .course(course)
                .selectionTime(LocalDateTime.now())
                .status(SelectionStatus.SELECTED)
                .build();

        Selection saved = selectionRepository.save(selection);

        // 更新课程已选人数
        course.setEnrolled(course.getEnrolled() + 1);

        return toDTO(saved);
    }

    /**
     * 退选课程
     */
    public void dropCourse(String studentNo, String courseCode) {
        Student student = studentService.findByStudentNo(studentNo);
        Course course = courseService.findByCourseCode(courseCode);

        Selection selection = selectionRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new EntityNotFoundException("未找到选课记录"));

        if (selection.getStatus() != SelectionStatus.SELECTED) {
            throw new IllegalStateException("当前状态不允许退选");
        }

        selection.setStatus(SelectionStatus.DROPPED);
        course.setEnrolled(Math.max(0, course.getEnrolled() - 1));
    }

    /** 查询学生所有选课 */
    @Transactional(readOnly = true)
    public List<SelectionDTO> findByStudent(String studentNo) {
        Student student = studentService.findByStudentNo(studentNo);
        return selectionRepository.findByStudentId(student.getId())
                .stream().map(SelectionService::toDTO).toList();
    }

    /** 查询学生已完成课程代码 */
    @Transactional(readOnly = true)
    public List<String> getCompletedCourseCodes(String studentNo) {
        Student student = studentService.findByStudentNo(studentNo);
        return selectionRepository.findCompletedCourseCodes(student.getId());
    }

    /** 实体转 DTO */
    public static SelectionDTO toDTO(Selection selection) {
        Course course = selection.getCourse();
        Student student = selection.getStudent();

        return SelectionDTO.builder()
                .selectionId(selection.getId())
                .selectionTime(selection.getSelectionTime())
                .status(selection.getStatus())
                .studentId(student.getId())
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getName())
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .credits(course.getCredits())
                .schedule(course.getSchedule())
                .location(course.getLocation())
                .category(course.getCategory())
                .semester(course.getSemester())
                .build();
    }
}
