package com.sqli.medwork.dto.response;

import com.sqli.medwork.enums.Role;
import lombok.Data;

@Data
public class AdminReadUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String matricule;
    private Role role;
    private boolean archived;
    private String dateCreation;
    private String lastLogin;
}

