package com.headhunt.service;

import com.headhunt.model.JobApplication;
import com.headhunt.model.ApplicationStatus;
import com.headhunt.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobApplicationService {
    
    private final JobApplicationRepository repository;
    private final CsvExportService csvExportService;

    @Autowired
    public JobApplicationService(JobApplicationRepository repository, CsvExportService csvExportService) {
        this.repository = repository;
        this.csvExportService = csvExportService;
    }

    public List<JobApplication> getAllApplications() {
        List<JobApplication> applications = repository.findAll();
        csvExportService.exportToCsv(applications);
        return applications;
    }

    public Optional<JobApplication> getApplicationById(Long id) {
        return repository.findById(id);
    }

    public JobApplication createApplication(JobApplication application) {
        JobApplication saved = repository.save(application);
        csvExportService.exportToCsv(repository.findAll());
        return saved;
    }

    public JobApplication updateApplication(Long id, JobApplication application) {
        if (repository.existsById(id)) {
            application.setId(id);
            JobApplication updated = repository.save(application);
            csvExportService.exportToCsv(repository.findAll());
            return updated;
        }
        throw new RuntimeException("Application not found with id: " + id);
    }

    public void deleteApplication(Long id) {
        repository.deleteById(id);
        csvExportService.exportToCsv(repository.findAll());
    }

    public List<JobApplication> searchByCompany(String companyName) {
        return repository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    public List<JobApplication> searchByPosition(String position) {
        return repository.findByPositionContainingIgnoreCase(position);
    }

    public List<JobApplication> getApplicationsByStatus(ApplicationStatus status) {
        return repository.findByStatus(status);
    }
} 