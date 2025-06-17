package com.headhunt.repository;

import com.headhunt.model.JobApplication;
import com.headhunt.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCompanyNameContainingIgnoreCase(String companyName);
    List<JobApplication> findByPositionContainingIgnoreCase(String position);
    List<JobApplication> findByStatus(ApplicationStatus status);
} 