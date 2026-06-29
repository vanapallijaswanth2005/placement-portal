package com.example.placementportal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.example.placementportal.entity.Student;
import com.example.placementportal.service.StudentService;
import com.example.placementportal.service.FileUploadService;
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
    private FileUploadService fileUploadService;

    // 🔒 Only RECRUITER or ADMIN can see list of all students
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    // 🎓 STUDENT: Get current student profile
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public Student getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentService.getStudentByName(username);
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
            String resumeUrl = fileUploadService.uploadResume(file);

            Student student = studentService.getStudentByName(username);
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
