-- Flyway V1: Initial Setup
-- This script represents the baseline schema generated from JPA entities.
-- Because spring.flyway.baseline-on-migrate=true is set, Flyway will mark this as applied automatically on existing databases.

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role ENUM('STUDENT', 'RECRUITER', 'ADMIN')
);

CREATE TABLE student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    branch VARCHAR(255),
    year VARCHAR(255),
    college VARCHAR(255),
    skills VARCHAR(255),
    cgpa DOUBLE,
    resume_url VARCHAR(255),
    linked_in VARCHAR(255),
    github VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE recruiter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruiter_name VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    designation VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    website_url VARCHAR(255),
    about_us TEXT,
    logo_url VARCHAR(255),
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    salary DOUBLE NOT NULL,
    description VARCHAR(255),
    location VARCHAR(255),
    experience VARCHAR(255),
    job_type VARCHAR(255),
    last_date DATE,
    skills VARCHAR(255),
    eligibility_criteria VARCHAR(255),
    recruiter_id BIGINT,
    FOREIGN KEY (recruiter_id) REFERENCES recruiter(id)
);

CREATE TABLE job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT,
    job_id BIGINT,
    status ENUM('APPLIED', 'UNDER_REVIEW', 'INTERVIEW', 'SELECTED', 'REJECTED'),
    interview_date DATETIME,
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (job_id) REFERENCES job(id),
    UNIQUE (student_id, job_id)
);

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL
);

CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
