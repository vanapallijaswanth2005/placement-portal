package com.example.placementportal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.example.placementportal.entity.Student;
import com.example.placementportal.service.StudentService;
import com.example.placementportal.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StorageService storageService;

    // 🔒 Only RECRUITER or ADMIN can see list of all students
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping
    public org.springframework.data.domain.Page<Student> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return studentService.getAllStudents(pageable);
    }

    // 🎓 STUDENT: Get current student profile
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public Student getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentService.getStudentByUsername(username);
    }

    // 🎓 STUDENT: Create or Update current student profile
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/me")
    public Student saveMyProfile(@Valid @RequestBody Student student) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentService.saveOrUpdateStudentForUser(username, student);
    }

    // 🎓 STUDENT: Upload resume PDF
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/me/resume")
    public Student uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String resumeUrl = storageService.uploadFile(file);

            Student student = studentService.getStudentByUsername(username);
            if (student == null) {
                throw new RuntimeException("Please create your profile first before uploading a resume");
            }
            student.setResumeUrl(resumeUrl);
            return studentService.saveStudent(student);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload resume: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Student createStudent(@Valid @RequestBody Student student) {
        return studentService.saveStudent(student);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @Valid @RequestBody Student student) {
        return studentService.updateStudent(id, student);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}
