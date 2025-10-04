package com.sqli.medwork.dto.request;

import com.sqli.medwork.validation.FutureDateTime;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for spontaneous visit requests from collaborators
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpontaneousVisitRequest {

    @NotBlank(message = "Le motif de la visite est obligatoire")
    private String reason;

    private String additionalNotes;

    @FutureDateTime(message = "La date et heure de la visite doivent être dans le futur", toleranceMinutes = 5)
    private LocalDateTime preferredDateTime;
} 