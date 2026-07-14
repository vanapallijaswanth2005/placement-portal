package com.example.placementportal.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ResumeParsingService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParsingService.class);

    // A predefined taxonomy of common tech skills
    private static final List<String> TECH_SKILLS = Arrays.asList(
            "Java", "Python", "C++", "C#", "JavaScript", "TypeScript",
            "HTML", "CSS", "React", "Angular", "Vue", "Node.js", "Spring Boot",
            "Django", "Flask", "Express", "SQL", "MySQL", "PostgreSQL", "MongoDB",
            "AWS", "Azure", "Google Cloud", "Docker", "Kubernetes", "Git",
            "Machine Learning", "Data Science", "AI", "React Native", "Flutter",
            "Swift", "Kotlin", "Android", "iOS", "Microservices", "REST API",
            "GraphQL", "Redis", "Kafka", "Linux", "Data Structures", "Algorithms"
    );

    /**
     * Extracts text from an uploaded PDF file.
     *
     * @param file The PDF MultipartFile
     * @return Extracted text as a String, or empty string if it fails
     */
    public String extractTextFromPdf(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return "";
        }

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
             
            if (document.isEncrypted()) {
                logger.warn("PDF is encrypted and cannot be parsed: {}", file.getOriginalFilename());
                return "";
            }

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (Exception e) {
            logger.error("Failed to parse PDF resume", e);
            return "";
        }
    }

    /**
     * Matches extracted text against a predefined list of tech skills.
     *
     * @param text The parsed text from the resume
     * @return A comma-separated string of identified skills
     */
    public String extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        Set<String> foundSkills = new HashSet<>();
        
        for (String skill : TECH_SKILLS) {
            // Regex to match the skill as a whole word (case insensitive)
            String regex = "(?i)\\b" + Pattern.quote(skill) + "\\b";
            if (Pattern.compile(regex).matcher(text).find()) {
                foundSkills.add(skill);
            }
        }

        return String.join(", ", foundSkills);
    }
}
