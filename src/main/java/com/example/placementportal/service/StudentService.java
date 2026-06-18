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

    public Student updateStudent(Long id, Student student) {
        Student existing = repo.findById(id).orElseThrow();

        existing.setName(student.getName());
        existing.setSkills(student.getSkills());
        existing.setCgpa(student.getCgpa());

        return repo.save(existing);
    }

    public void deleteStudent(Long id) {
        repo.deleteById(id);
    }
}