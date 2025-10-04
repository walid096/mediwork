package com.sqli.medwork.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entity representing a recurring weekly availability pattern for doctors
 */
@Entity
@Table(
        name = "recurring_slots",
        indexes = {
                @Index(name = "idx_recurring_doctor", columnList = "doctor_id"),
                @Index(name = "idx_recurring_day", columnList = "day_of_week"),
                @Index(name = "idx_recurring_doctor_day", columnList = "doctor_id, day_of_week")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;              // Doctor with recurring availability

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;      // Day of the week (Monday, Tuesday, etc.)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;      // Weekly start time (e.g., 09:00)

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;        // Weekly end time (e.g., 17:00)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isValidTimeRange() {
        return startTime.isBefore(endTime);
    }

    public boolean overlapsWith(RecurringSlot other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return !(this.endTime.isBefore(other.startTime) || other.endTime.isBefore(this.startTime));
    }
}