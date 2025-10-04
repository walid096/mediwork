package com.sqli.medwork.controller.auth;

import com.sqli.medwork.dto.response.LoginResponse;
import com.sqli.medwork.dto.request.LoginRequest;
import com.sqli.medwork.dto.request.RegisterRequest;
import com.sqli.medwork.dto.request.RefreshTokenRequest;
import com.sqli.medwork.dto.response.RefreshTokenResponse;
import com.sqli.medwork.service.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authService;

    /**
     * Register a new user
     * @param request RegisterRequest containing user details
     * @return LoginResponse with access token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        LoginResponse response = authService.register(request);
        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Login user with email and password
     * @param request LoginRequest containing credentials
     * @return LoginResponse with access token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     * @param request RefreshTokenRequest containing refresh token
     * @return RefreshTokenResponse with new access token and refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        RefreshTokenResponse response = authService.refreshToken(request);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by revoking refresh token
     * @param request RefreshTokenRequest containing refresh token to revoke
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");
        authService.logout(request.getRefreshToken());
        log.info("User logged out successfully");
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Logout user from all devices (requires authentication)
     * @param refreshToken Refresh token from request header or body
     * @return Success message
     */
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAllDevices(@RequestParam String refreshToken) {
        log.info("Logout all devices request received");

        // Get user from refresh token
        var userOptional = authService.getUserByRefreshToken(refreshToken);
        if (userOptional.isPresent()) {
            authService.logoutAllDevices(userOptional.get());
            log.info("User logged out from all devices: {}", userOptional.get().getEmail());
            return ResponseEntity.ok("Logged out from all devices successfully");
        } else {
            log.warn("Invalid refresh token provided for logout all devices");
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }

    /**
     * Validate refresh token (health check endpoint)
     * @param refreshToken Refresh token to validate
     * @return Validation result
     */
    @GetMapping("/validate-token")
    public ResponseEntity<String> validateToken(@RequestParam String refreshToken) {
        log.info("Token validation request received");
        boolean isValid = authService.isRefreshTokenValid(refreshToken);

        if (isValid) {
            log.info("Token validation successful");
            return ResponseEntity.ok("Token is valid");
        } else {
            log.warn("Token validation failed");
            return ResponseEntity.status(401).body("Token is invalid");
        }
    }
}