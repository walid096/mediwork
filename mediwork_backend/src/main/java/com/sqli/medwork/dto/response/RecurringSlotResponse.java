package com.sqli.medwork.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;  // ✅ ADD THIS IMPORT
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringSlotResponse {

    private Long id;
    private VisitResponse.UserInfoDto doctor;
    private DayOfWeek dayOfWeek;

    @JsonFormat(pattern = "HH:mm")  // ✅ ADD THIS ANNOTATION
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")  // ✅ ADD THIS ANNOTATION
    private LocalTime endTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}