package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user selection in dropdowns and forms
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSelectionDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String matricule;
    private Role role;
    private Boolean isActive;

    // ---------------- Helper Methods ----------------

    public boolean isDoctor() {
        return Role.DOCTOR.equals(role);
    }

    public boolean isHR() {
        return Role.RH.equals(role);
    }

    public boolean isCollaborator() {
        return Role.COLLABORATOR.equals(role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    public boolean canBeSelectedForVisits() {
        return isActive != null && isActive && (isDoctor() || isCollaborator());
    }

    public String getDisplayName() {
        if (isDoctor()) return "Dr. " + lastName + " (" + firstName + ")";
        if (isHR()) return firstName + " " + lastName + " (RH)";
        if (isAdmin()) return firstName + " " + lastName + " (Admin)";
        return firstName + " " + lastName;
    }

    public String getCompactDisplayName() {
        if (isDoctor()) return "Dr. " + lastName;
        return firstName + " " + lastName;
    }

    public String getSearchableText() {
        return String.format("%s %s %s %s",
                firstName != null ? firstName : "",
                lastName != null ? lastName : "",
                email != null ? email : "",
                matricule != null ? matricule : ""
        ).toLowerCase().trim();
    }

    public boolean matchesSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return true;
        return getSearchableText().contains(searchTerm.toLowerCase().trim());
    }
}
