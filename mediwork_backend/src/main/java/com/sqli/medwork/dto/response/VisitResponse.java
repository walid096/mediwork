package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.VisitStatus;
import com.sqli.medwork.enums.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * DTO for API responses containing visit information.
 *
 * Provides comprehensive visit details for:
 * - Frontend display and management
 * - API responses to client requests
 * - Data transfer between service layers
 *
 * Design Principles:
 * - Immutable data structure for consistency
 * - Rich information for complete user experience
 * - Formatted data ready for frontend consumption
 * - Audit trail for tracking changes
 *
 * Usage:
 * - Returned after successful visit creation
 * - Used in visit listing and detail views
 * - Supports both HR and doctor dashboards
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitResponse {

    /** Unique identifier for the visit */
    private Long id;

    /** Collaborator (employee) information */
    private UserInfoDto collaborator;

    /** Doctor information */
    private UserInfoDto doctor;

    /** Time slot details */
    private SlotInfoDto slot;

    /** Type of medical visit */
    private VisitType visitType;

    /** Current status of the visit */
    private VisitStatus status;

    /** HR user who created the visit */
    private UserInfoDto createdBy;

    /** When the visit was created */
    private LocalDateTime createdAt;

    /** When the visit was last updated */
    private LocalDateTime updatedAt;

    // ---------------- Helper Methods ----------------

    /** Check if visit is pending doctor confirmation */
    public boolean isPendingConfirmation() {
        return VisitStatus.PENDING_DOCTOR_CONFIRMATION.equals(status);
    }

    /** Check if visit is confirmed and scheduled */
    public boolean isConfirmed() {
        return VisitStatus.SCHEDULED.equals(status);
    }

    /** Check if visit is in progress */
    public boolean isInProgress() {
        return VisitStatus.IN_PROGRESS.equals(status);
    }

    /** Check if visit is completed */
    public boolean isCompleted() {
        return VisitStatus.COMPLETED.equals(status);
    }

    /** Check if visit can be cancelled */
    public boolean canBeCancelled() {
        return VisitStatus.PENDING_DOCTOR_CONFIRMATION.equals(status) ||
                VisitStatus.SCHEDULED.equals(status);
    }

    /** Get formatted visit date and time for frontend display */
    public String getFormattedDateTime() {
        if (slot != null && slot.getStartTime() != null) {
            return slot.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "Date not set";
    }

    /** Get visit duration in minutes */
    public int getDurationMinutes() {
        if (slot != null && slot.getStartTime() != null && slot.getEndTime() != null) {
            return (int) Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();  // ✅ FIXED: Duration usage
        }
        return 0;
    }

    /** Get a summary of the visit for logging or display */
    public String getVisitSummary() {
        return String.format("Visit #%d: %s - %s with Dr. %s on %s",
                id,
                visitType,
                collaborator != null ? collaborator.getFullName() : "Unknown",
                doctor != null ? doctor.getFullName() : "Unknown",
                getFormattedDateTime()
        );
    }

    // ---------------- Nested DTOs ----------------

    /**
     * DTO for user information in visit responses
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String matricule;

        /** Get full name for display */
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    /**
     * DTO for slot information in visit responses
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfoDto {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;

        /** Get formatted time range for display */
        public String getTimeRange() {
            if (startTime != null && endTime != null) {
                return startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                        " - " +
                        endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            }
            return "Time not set";
        }
    }
}