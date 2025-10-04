package com.sqli.medwork.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String fullName;
    private String role;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;  // Optional: token expiration time in seconds
}
