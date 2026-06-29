package com.example.placementportal.service;

import com.example.placementportal.entity.Student;
import com.example.placementportal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repo;

    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    public Student saveStudent(Student student) {
        return repo.save(student);
    }

    public Student getStudentByName(String name) {
        return repo.findByName(name).orElse(null);
    }

    public Student getStudentById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Student saveOrUpdateStudentForUser(String username, Student student) {
        Student existing = repo.findByName(username).orElse(null);
        if (existing == null) {
            existing = new Student();
            existing.setName(username);
        }
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