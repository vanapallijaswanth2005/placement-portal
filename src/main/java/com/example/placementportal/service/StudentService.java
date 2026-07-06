package com.example.placementportal.service;

import com.example.placementportal.entity.Student;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository repo;

    @Autowired
    private UserRepository userRepo;

    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    public org.springframework.data.domain.Page<Student> getAllStudents(org.springframework.data.domain.Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Student saveStudent(Student student) {
        return repo.save(student);
    }

    public Student getStudentByName(String name) {
        return repo.findByName(name).orElse(null);
    }

    public Student getStudentByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Student student = repo.findByUserId(user.getId()).orElse(null);
        if (student != null) {
            return student;
        }

        Student legacyStudent = repo.findByName(username).orElse(null);
        if (legacyStudent != null) {
            legacyStudent.setUser(user);
            return repo.save(legacyStudent);
        }

        return null;
    }

    public Student getStudentById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public String getStudentUserEmail(Long studentId) {
        return repo.findUserEmailByStudentId(studentId).orElse(null);
    }

    public Student saveOrUpdateStudentForUser(String username, Student student) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Student existing = repo.findByUserId(user.getId()).orElse(null);
        if (existing == null) {
            existing = repo.findByName(username).orElse(new Student());
            existing.setUser(user);
        }
        existing.setName(username);
        existing.setEmail(student.getEmail());
        existing.setPhone(student.getPhone());
        existing.setBranch(student.getBranch());
        existing.setYear(student.getYear());
        existing.setCollege(student.getCollege());
        existing.setSkills(student.getSkills());
        existing.setCgpa(student.getCgpa());
        existing.setLinkedIn(student.getLinkedIn());
        existing.setGithub(student.getGithub());
        // resumeUrl is set separately via file upload
        return repo.save(existing);
    }

    public Student updateStudent(Long id, Student student) {
        Student existing = repo.findById(id).orElseThrow();

        existing.setName(student.getName());
        existing.setEmail(student.getEmail());
        existing.setPhone(student.getPhone());
        existing.setBranch(student.getBranch());
        existing.setYear(student.getYear());
        existing.setCollege(student.getCollege());
        existing.setSkills(student.getSkills());
        existing.setCgpa(student.getCgpa());
        existing.setLinkedIn(student.getLinkedIn());
        existing.setGithub(student.getGithub());

        return repo.save(existing);
    }

    public void deleteStudent(@org.springframework.lang.NonNull Long id) {
        repo.deleteById(id);
    }
}
