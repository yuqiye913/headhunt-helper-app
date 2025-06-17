package com.headhunt.controller;

import com.headhunt.model.JobApplication;
import com.headhunt.model.ApplicationStatus;
import com.headhunt.service.JobApplicationService;
import com.headhunt.service.JobExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*", methods = {
    RequestMethod.GET,
    RequestMethod.POST,
    RequestMethod.PUT,
    RequestMethod.DELETE
}, allowedHeaders = {
    "Content-Type",
    "Authorization",
    "X-Requested-With"
})
public class JobApplicationController {
    private static final Logger logger = LoggerFactory.getLogger(JobApplicationController.class);

    private final JobApplicationService service;
    private final JobExtractorService jobExtractorService;

    @Autowired
    public JobApplicationController(JobApplicationService service, JobExtractorService jobExtractorService) {
        this.service = service;
        this.jobExtractorService = jobExtractorService;
    }

    @GetMapping
    public List<JobApplication> getAllApplications() {
        logger.debug("Received GET request for all applications");
        return service.getAllApplications();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplication> getApplicationById(@PathVariable Long id) {
        logger.debug("Received GET request for application with ID: {}", id);
        return service.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public JobApplication createApplication(@RequestBody JobApplication application) {
        logger.debug("Received POST request to create application: {}", application);
        return service.createApplication(application);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplication> updateApplication(
            @PathVariable Long id,
            @RequestBody JobApplication application) {
        logger.debug("Received PUT request to update application with ID: {}, data: {}", id, application);
        try {
            JobApplication updated = service.updateApplication(id, application);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("Failed to update application with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        logger.debug("Received DELETE request for application with ID: {}", id);
        service.deleteApplication(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search/company")
    public List<JobApplication> searchByCompany(@RequestParam String companyName) {
        logger.debug("Received GET request to search by company name: {}", companyName);
        return service.searchByCompany(companyName);
    }

    @GetMapping("/search/position")
    public List<JobApplication> searchByPosition(@RequestParam String position) {
        logger.debug("Received GET request to search by position: {}", position);
        return service.searchByPosition(position);
    }

    @GetMapping("/status/{status}")
    public List<JobApplication> getApplicationsByStatus(@PathVariable ApplicationStatus status) {
        logger.debug("Received GET request to get applications by status: {}", status);
        return service.getApplicationsByStatus(status);
    }

    @PostMapping("/html")
    public ResponseEntity<?> processHtmlContent(@RequestBody String htmlContent) {
        logger.debug("Received POST request to process HTML content, length: {}", htmlContent.length());
        try {
            Map<String, Object> extractedData = jobExtractorService.extractAndCreateJob(htmlContent);
            logger.debug("Successfully processed HTML and created job application: {}", extractedData);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(extractedData);
        } catch (Exception e) {
            logger.error("Error processing HTML content", e);
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "Error processing HTML: " + e.getMessage()));
        }
    }
} 