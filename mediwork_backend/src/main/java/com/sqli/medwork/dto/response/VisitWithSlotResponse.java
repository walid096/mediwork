package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.VisitStatus;
import com.sqli.medwork.enums.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO response for creating a visit with a new slot
 * 
 * Contains both the created visit and slot information
 * Used to confirm successful creation and provide details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitWithSlotResponse {

    /** Created visit information */
    private VisitInfo visit;

    /** Created slot information */
    private SlotInfo slot;

    /** Creation timestamp */
    private LocalDateTime createdAt;

    /** Summary of the operation */
    public String getOperationSummary() {
        return String.format("Created visit %d with slot %d for %s on %s",
                visit.getId(), slot.getId(), visit.getCollaboratorName(), slot.getFormattedDateTime());
    }

    // ==================== INNER CLASSES ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitInfo {
        private Long id;
        private String collaboratorName;
        private String doctorName;
        private VisitType visitType;
        private VisitStatus status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfo {
        private Long id;
        private String doctorName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private LocalDateTime createdAt;

        /** Get formatted date and time for display */
        public String getFormattedDateTime() {
            if (startTime != null && endTime != null) {
                return String.format("%s - %s", 
                    startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            }
            return "N/A";
        }

        /** Get duration in minutes */
        public long getDurationInMinutes() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMinutes();
            }
            return 0;
        }
    }
} 