package com.sqli.medwork.dto.request;

import com.sqli.medwork.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String password; // Optionnel, si l'admin veut changer le mot de passe

    @NotBlank(message = "Matricule is required")
    private String matricule;

    private Role role; // Optionnel, si l'admin veut changer le rôle
}