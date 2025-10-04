package com.sqli.medwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for doctor information displayed to RH users
 * 
 * Contains essential information needed for visit scheduling:
 * - Basic identification (ID, name, email, matricule)
 * - Professional information
 * - Excludes sensitive data like password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfoResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String matricule;
    private LocalDateTime dateCreation;
    private LocalDateTime lastLogin;
    private boolean archived;

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Helper method to check if doctor is available
    public boolean isAvailable() {
        return !archived;
    }
} 