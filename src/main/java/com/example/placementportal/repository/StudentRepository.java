package com.example.placementportal.repository;

import com.example.placementportal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByName(String name);
    Optional<Student> findByUserId(Long userId);

    @Query("select s.user.email from Student s where s.id = :studentId")
    Optional<String> findUserEmailByStudentId(@Param("studentId") Long studentId);
}
