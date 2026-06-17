package com.courseselect.service;

import com.courseselect.entity.Student;
import com.courseselect.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("学生不存在: " + id));
    }

    @Transactional(readOnly = true)
    public Student findByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new EntityNotFoundException("学生不存在: " + studentNo));
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void deleteById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("学生不存在: " + id);
        }
        studentRepository.deleteById(id);
    }
}
