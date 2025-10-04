package com.sqli.medwork.dto.request;

import com.sqli.medwork.enums.SlotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSlotStatusRequest {

    @NotNull(message = "Status is required")
    private SlotStatus status;
}