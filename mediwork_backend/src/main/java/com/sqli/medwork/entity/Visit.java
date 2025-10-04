package com.sqli.medwork.entity;

import com.sqli.medwork.enums.VisitStatus;
import com.sqli.medwork.enums.VisitType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a medical visit appointment
 */
@Entity
@Table(
        name = "visits",
        indexes = {
                @Index(name = "idx_collaborator", columnList = "collaborator_id"),
                @Index(name = "idx_doctor", columnList = "doctor_id"),
                @Index(name = "idx_slot", columnList = "slot_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_by", columnList = "created_by"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private User collaborator;        // Employee requesting the visit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;              // Medical professional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = true)
    private Slot slot;                // Time slot for appointment (nullable for spontaneous requests)

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false)
    private VisitType visitType;      // Type of medical visit

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status;       // Current visit status

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;           // HR user who scheduled

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isPendingConfirmation() {
        return status == VisitStatus.PENDING_DOCTOR_CONFIRMATION;
    }

    public boolean isConfirmed() {
        return status == VisitStatus.SCHEDULED;
    }

    public boolean isCompleted() {
        return status == VisitStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == VisitStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return status == VisitStatus.PENDING_DOCTOR_CONFIRMATION ||
                status == VisitStatus.SCHEDULED;
    }
}