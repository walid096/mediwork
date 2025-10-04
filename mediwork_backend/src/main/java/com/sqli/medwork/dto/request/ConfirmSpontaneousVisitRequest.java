package com.sqli.medwork.dto.request;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for HR/Admin to confirm a spontaneous visit request or change its date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSpontaneousVisitRequest {

    // Optional: if provided, overrides the preferred date/time and confirms the request
    @Future(message = "La date et heure doivent être dans le futur")
    private LocalDateTime newDateTime;
}
