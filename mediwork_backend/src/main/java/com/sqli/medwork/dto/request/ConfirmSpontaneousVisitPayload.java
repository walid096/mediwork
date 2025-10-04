package com.sqli.medwork.dto.request;

import com.sqli.medwork.enums.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Optional JSON body for confirming a spontaneous visit, as an alternative to query params.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSpontaneousVisitPayload {
    private Long doctorId;
    private LocalDateTime dateTime; // Overrides preferredDateTime if provided
    private VisitType visitType;    // Defaults to SPONTANEOUS if null
}
