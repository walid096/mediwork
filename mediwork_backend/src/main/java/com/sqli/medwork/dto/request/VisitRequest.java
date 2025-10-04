package com.sqli.medwork.dto.request;

import com.sqli.medwork.enums.VisitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for HR to request scheduling a medical visit.
 * Captures all necessary information to create a Visit entity.
 *
 * Business Rules:
 * - All fields are mandatory
 * - IDs must be positive numbers
 * - Visit type must be valid enum value
 *
 * Security:
 * - HR role validation happens in service layer
 * - User existence validation happens in service layer
 * - Slot availability validation happens in service layer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitRequest {

    /** ID of the collaborator requiring the medical visit (role = COLLABORATOR) */
    @NotNull(message = "Collaborator ID is required")
    @Positive(message = "Collaborator ID must be positive")
    private Long collaboratorId;

    /** ID of the doctor who will conduct the visit (role = DOCTOR) */
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;

    /** ID of the time slot for the visit (status = AVAILABLE) */
    @NotNull(message = "Slot ID is required")
    @Positive(message = "Slot ID must be positive")
    private Long slotId;

    /** Type of medical visit being scheduled */
    @NotNull(message = "Visit type is required")
    private VisitType visitType;

    /** Summary for logging and audit */
    public String getRequestSummary() {
        return String.format("Visit Request: Type=%s, Collaborator=%d, Doctor=%d, Slot=%d",
                visitType, collaboratorId, doctorId, slotId);
    }
}