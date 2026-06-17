package com.courseselect.controller;

import com.courseselect.dto.ApiResponse;
import com.courseselect.dto.CourseDTO;
import com.courseselect.entity.Course;
import com.courseselect.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** 获取所有课程 */
    @GetMapping
    public ApiResponse<List<CourseDTO>> list(@RequestParam(required = false) String semester,
                                              @RequestParam(required = false) String category) {
        List<Course> courses;
        if (semester != null && category != null) {
            courses = courseService.findBySemesterAndCategory(semester, category);
        } else if (semester != null) {
            courses = courseService.findBySemester(semester);
        } else {
            courses = courseService.findAll();
        }

        List<CourseDTO> dtos = courses.stream().map(CourseService::toDTO).toList();
        return ApiResponse.ok(dtos);
    }

    /** 获取该学期有余额的课程 */
    @GetMapping("/available")
    public ApiResponse<List<CourseDTO>> available(@RequestParam String semester) {
        List<Course> courses = courseService.findAvailable(semester);
        return ApiResponse.ok(courses.stream().map(CourseService::toDTO).toList());
    }

    /** 按课程代码查询 */
    @GetMapping("/{courseCode}")
    public ApiResponse<CourseDTO> getByCode(@PathVariable String courseCode) {
        Course course = courseService.findByCourseCode(courseCode);
        return ApiResponse.ok(CourseService.toDTO(course));
    }
}
