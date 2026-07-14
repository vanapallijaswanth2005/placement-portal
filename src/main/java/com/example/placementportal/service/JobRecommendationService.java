package com.example.placementportal.service;

import com.example.placementportal.entity.Job;
import com.example.placementportal.entity.Student;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobRecommendationService {

    /**
     * Recommends jobs for a student based on skill and branch matching.
     * Uses a lightweight NLP approach (Token Overlap / Jaccard Index similarity).
     */
    public List<Job> recommendJobs(Student student, List<Job> allJobs, int limit) {
        if (student == null || allJobs == null || allJobs.isEmpty()) {
            return Collections.emptyList();
        }

        // Tokenize student profile (skills + branch)
        String studentText = (student.getSkills() != null ? student.getSkills() : "") + " " +
                             (student.getBranch() != null ? student.getBranch() : "");
        Set<String> studentTokens = tokenize(studentText);
        
        if (studentTokens.isEmpty()) {
            // If student has no skills or branch, just return latest jobs
            return allJobs.stream()
                    .sorted((j1, j2) -> j2.getId().compareTo(j1.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // Calculate score for each job
        Map<Job, Double> jobScores = new HashMap<>();
        for (Job job : allJobs) {
            String jobText = (job.getTitle() != null ? job.getTitle() : "") + " " +
                             (job.getSkills() != null ? job.getSkills() : "") + " " +
                             (job.getDescription() != null ? job.getDescription() : "");
                             
            Set<String> jobTokens = tokenize(jobText);
            double score = calculateJaccardSimilarity(studentTokens, jobTokens);
            
            // Weighting: If job explicitly matches the student's branch in eligibility criteria
            if (job.getEligibilityCriteria() != null && student.getBranch() != null &&
                !student.getBranch().isBlank() &&
                job.getEligibilityCriteria().toLowerCase().contains(student.getBranch().toLowerCase())) {
                score += 0.2; // Boost score for branch match
            }

            // CGPA Boost: Extract CGPA from eligibility criteria if it exists (e.g. "7.5 CGPA")
            if (job.getEligibilityCriteria() != null && job.getEligibilityCriteria().toLowerCase().contains("cgpa")) {
                try {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(\\.\\d+)?)").matcher(job.getEligibilityCriteria());
                    if (m.find()) {
                        double requiredCgpa = Double.parseDouble(m.group(1));
                        if (student.getCgpa() >= requiredCgpa) {
                            score += 0.3; // Boost score for meeting CGPA criteria
                        } else {
                            score -= 0.2; // Penalize if CGPA is lower than required
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }
            }
            
            jobScores.put(job, score);
        }

        // Sort by score descending and return top 'limit'
        return jobScores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.0) // Only return jobs with some relevance
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return new HashSet<>();
        }
        // Lowercase, remove punctuation (keeping + and # for C++, C#), split by whitespace
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-z0-9+#]", " ").split("\\s+"))
                .filter(s -> !s.isBlank() && s.length() > 1) 
                .collect(Collectors.toSet());
    }

    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
}
