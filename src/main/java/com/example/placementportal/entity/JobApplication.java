package com.example.placementportal.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "job_id"}))
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private java.time.LocalDateTime interviewDate;

    public JobApplication() {}

    public java.time.LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(java.time.LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
