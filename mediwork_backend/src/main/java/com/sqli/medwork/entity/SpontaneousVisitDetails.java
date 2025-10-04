package com.sqli.medwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.sqli.medwork.enums.SchedulingStatus;

/**
 * Entity for storing additional details of spontaneous visit requests
 */
@Entity
@Table(name = "spontaneous_visit_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpontaneousVisitDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Detached from Visit: only linked to collaborator who created the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private User collaborator;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "preferred_date_time")
    private LocalDateTime preferredDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheduling_status", length = 30)
    private SchedulingStatus schedulingStatus;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 