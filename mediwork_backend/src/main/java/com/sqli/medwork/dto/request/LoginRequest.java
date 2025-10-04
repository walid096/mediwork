package com.sqli.medwork.dto.request;
import lombok.*;
import jakarta.validation.constraints.Pattern;

@Data
public class LoginRequest {
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email invalide")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,}$", message = "Mot de passe invalide")
    private String password;
}
