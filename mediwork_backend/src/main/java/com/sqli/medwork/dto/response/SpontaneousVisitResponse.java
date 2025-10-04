package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.SchedulingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for spontaneous visit request responses (detached from Visit)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpontaneousVisitResponse {

    private Long id;
    private String reason;
    private String additionalNotes;
    private String collaboratorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Preferred date/time information
    private LocalDateTime preferredDateTime;
    private SchedulingStatus schedulingStatus;
}