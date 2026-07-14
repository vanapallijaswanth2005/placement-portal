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

    @Column(name = "match_score")
    private Double matchScore;

    public JobApplication() {}

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    @Transient
    public String getGoogleCalendarUrl() {
        if (interviewDate != null && job != null) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
                String start = interviewDate.format(formatter);
                String end = interviewDate.plusHours(1).format(formatter);
                String title = java.net.URLEncoder.encode("Interview with " + job.getCompany() + " for " + job.getTitle(), java.nio.charset.StandardCharsets.UTF_8);
                return "https://calendar.google.com/calendar/render?action=TEMPLATE&text=" + title + "&dates=" + start + "Z/" + end + "Z";
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

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
