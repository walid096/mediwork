package com.sqli.medwork.dto.request;

import com.sqli.medwork.enums.VisitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for HR to create a visit with a new slot at the same time
 * 
 * This allows RH users to schedule visits by creating slots on-demand
 * instead of only using pre-existing available slots
 * 
 * Business Rules:
 * - All fields are mandatory
 * - IDs must be positive numbers
 * - Visit type must be valid enum value
 * - Start time must be in the future
 * - End time must be after start time
 * - Duration must be reasonable (e.g., 15 minutes to 2 hours)
 * 
 * Security:
 * - HR role validation happens in service layer
 * - User existence validation happens in service layer
 * - Slot conflict validation happens in service layer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVisitWithSlotRequest {

    /** ID of the collaborator requiring the medical visit (role = COLLABORATOR) */
    @NotNull(message = "Collaborator ID is required")
    @Positive(message = "Collaborator ID must be positive")
    private Long collaboratorId;

    /** ID of the doctor who will conduct the visit (role = DOCTOR) */
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;

    /** Type of medical visit being scheduled */
    @NotNull(message = "Visit type is required")
    private VisitType visitType;

    /** Start time for the new slot */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    /** End time for the new slot */
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    /** Summary for logging and audit */
    public String getRequestSummary() {
        return String.format("Visit with Slot Request: Type=%s, Collaborator=%d, Doctor=%d, Time=%s to %s",
                visitType, collaboratorId, doctorId, startTime, endTime);
    }

    /** Validate that start time is in the future */
    public boolean isStartTimeInFuture() {
        // Add a small buffer (5 minutes) to account for timezone differences and processing time
        LocalDateTime nowWithBuffer = LocalDateTime.now().minusMinutes(5);
        return startTime != null && startTime.isAfter(nowWithBuffer);
    }

    /** Validate that end time is after start time */
    public boolean isTimeRangeValid() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /** Get duration in minutes */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /** Validate duration is reasonable (15 minutes to 2 hours) */
    public boolean isDurationReasonable() {
        long duration = getDurationInMinutes();
        return duration >= 15 && duration <= 120; // 15 minutes to 2 hours
    }
} 