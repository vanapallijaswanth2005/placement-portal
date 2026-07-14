package com.example.placementportal.service;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.entity.Student;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
public class CsvExportService {

    public void exportApplicationsToCsv(PrintWriter writer, List<JobApplication> applications) {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            // Write Header
            String[] header = {
                    "Application ID",
                    "Student Name",
                    "Email",
                    "Phone",
                    "College",
                    "Branch",
                    "Year of Study",
                    "CGPA",
                    "Skills",
                    "Status",
                    "Applied At"
            };
            csvWriter.writeNext(header);

            // Write Data
            for (JobApplication app : applications) {
                Student student = app.getStudent();
                String[] data = {
                        String.valueOf(app.getId()),
                        student != null ? student.getName() : "N/A",
                        student != null ? student.getEmail() : "N/A",
                        student != null && student.getPhone() != null ? student.getPhone() : "N/A",
                        student != null && student.getCollege() != null ? student.getCollege() : "N/A",
                        student != null && student.getBranch() != null ? student.getBranch() : "N/A",
                        student != null && student.getYear() != null ? student.getYear() : "N/A",
                        student != null ? String.valueOf(student.getCgpa()) : "N/A",
                        student != null && student.getSkills() != null ? student.getSkills() : "N/A",
                        app.getStatus() != null ? app.getStatus().name() : "N/A",
                        "N/A" // createdAt not available in entity
                };
                csvWriter.writeNext(data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while writing CSV", e);
        }
    }
}
