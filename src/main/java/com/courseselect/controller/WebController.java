package com.courseselect.controller;

import com.courseselect.entity.Student;
import com.courseselect.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 前端页面路由控制器
 */
@Controller
public class WebController {

    private final StudentService studentService;

    public WebController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Student> students = studentService.findAll();
        model.addAttribute("students", students);
        return "index";
    }

    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }

    @GetMapping("/select")
    public String select(Model model) {
        List<Student> students = studentService.findAll();
        model.addAttribute("students", students);
        return "select";
    }

    @GetMapping("/my")
    public String myCourses() {
        return "my";
    }

    @GetMapping("/agent")
    public String agent(Model model) {
        List<Student> students = studentService.findAll();
        model.addAttribute("students", students);
        return "agent";
    }
}
