package com.sqli.medwork.entity;

import com.sqli.medwork.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a medical appointment time slot
 */
@Entity
@Table(
        name = "slots",
        indexes = {
                @Index(name = "idx_doctor", columnList = "doctor_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_start_time", columnList = "start_time"),
                @Index(name = "idx_doctor_status", columnList = "doctor_id, status")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;              // Doctor providing the slot

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;  // Slot start time

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;    // Slot end time

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;        // Current slot status

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    public boolean isBooked() {
        return status == SlotStatus.CONFIRMED || status == SlotStatus.TEMPORARILY_LOCKED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }
}