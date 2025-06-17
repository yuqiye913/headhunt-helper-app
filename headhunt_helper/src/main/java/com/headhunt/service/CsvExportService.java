package com.headhunt.service;

import com.headhunt.model.JobApplication;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {
    private static final String CSV_FILE_PATH = "job_applications.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void exportToCsv(List<JobApplication> applications) {
        try (FileWriter writer = new FileWriter(CSV_FILE_PATH)) {
            // Write header
            writer.write("ID,Company Name,Position,Job URL,Status,Applied Time,Location,Salary,Contact Person,Contact Email,Notes\n");

            // Write data
            for (JobApplication app : applications) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    app.getId(),
                    escapeCsvField(app.getCompanyName()),
                    escapeCsvField(app.getPosition()),
                    escapeCsvField(app.getJobUrl()),
                    app.getStatus(),
                    app.getAppliedTime().format(DATE_FORMATTER),
                    escapeCsvField(app.getLocation()),
                    escapeCsvField(app.getSalary()),
                    escapeCsvField(app.getContactPerson()),
                    escapeCsvField(app.getContactEmail()),
                    escapeCsvField(app.getNotes())
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        // If field contains comma, quote, or newline, wrap in quotes and escape existing quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
} 