package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO for API responses containing slot information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private Long id;
    private DoctorInfoDto doctor;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SlotStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ---------------- Helper Methods ----------------

    public boolean isAvailable() {
        return SlotStatus.AVAILABLE.equals(status);
    }

    public boolean isTemporarilyLocked() {
        return SlotStatus.TEMPORARILY_LOCKED.equals(status);
    }

    public boolean isConfirmed() {
        return SlotStatus.CONFIRMED.equals(status);
    }

    public boolean isUnavailable() {
        return SlotStatus.UNAVAILABLE.equals(status);
    }

    public boolean isExpired() {
        return startTime != null && startTime.isBefore(LocalDateTime.now());
    }

    public boolean isToday() {
        if (startTime == null) return false;
        return startTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    public boolean isTomorrow() {
        if (startTime == null) return false;
        return startTime.toLocalDate().equals(LocalDateTime.now().toLocalDate().plusDays(1));
    }

    public String getFormattedDate() {
        if (startTime == null) return "Date not set";
        if (isToday()) return "Today";
        if (isTomorrow()) return "Tomorrow";
        return startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedTimeRange() {
        if (startTime == null || endTime == null) return "Time not set";
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " +
                endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedDateTime() {
        return getFormattedDate() + " " + getFormattedTimeRange();
    }

    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    public boolean canBeSelectedByHR() {
        return isAvailable() && !isExpired();
    }

    public boolean canBeManagedByDoctor() {
        return !isConfirmed() && !isExpired();
    }

    public String getSlotSummary() {
        return String.format("Slot #%d: %s - Dr. %s on %s (%s)",
                id,
                getFormattedTimeRange(),
                doctor != null ? doctor.getFullName() : "Unknown",
                getFormattedDate(),
                status
        );
    }

    // ---------------- Nested DTO ----------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfoDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String matricule;
        private String specialization;

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
