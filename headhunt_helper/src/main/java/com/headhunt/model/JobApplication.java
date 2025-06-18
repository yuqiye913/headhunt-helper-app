package com.headhunt.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String companyName;

    @Column(nullable = false, length = 255)
    private String position;

    @Column(nullable = false, length = 1000)
    private String jobUrl;

    @Column(length = 1000)
    private String jobWebsite;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime appliedTime;

    @Column
    private LocalDateTime appliedDate;

    @Column(length = 4000)
    private String notes;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String salary;

    @Column(length = 255)
    private String contactPerson;

    @Column(length = 255)
    private String contactEmail;

    @PrePersist
    protected void onCreate() {
        appliedTime = LocalDateTime.now();
    }
} 