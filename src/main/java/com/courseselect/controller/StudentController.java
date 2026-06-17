package com.courseselect.controller;

import com.courseselect.dto.ApiResponse;
import com.courseselect.entity.Student;
import com.courseselect.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<List<Student>> list() {
        return ApiResponse.ok(studentService.findAll());
    }

    @GetMapping("/{studentNo}")
    public ApiResponse<Student> getByNo(@PathVariable String studentNo) {
        return ApiResponse.ok(studentService.findByStudentNo(studentNo));
    }

    @PostMapping
    public ApiResponse<Student> create(@RequestBody Student student) {
        return ApiResponse.ok(studentService.save(student));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.deleteById(id);
        return ApiResponse.ok(null);
    }
}
