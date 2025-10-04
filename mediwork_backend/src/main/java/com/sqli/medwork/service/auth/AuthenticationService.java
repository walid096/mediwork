package com.sqli.medwork.service.auth;

import com.sqli.medwork.dto.request.LoginRequest;
import com.sqli.medwork.dto.response.LoginResponse;
import com.sqli.medwork.dto.request.RefreshTokenRequest;
import com.sqli.medwork.dto.response.RefreshTokenResponse;
import com.sqli.medwork.dto.request.RegisterRequest;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.entity.RefreshToken;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.exception.*;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.service.common.LogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final LogService logService;

    // Max allowed failed attempts before locking
    private static final int MAX_FAILED_ATTEMPTS = 3;
    // Lock duration in minutes
    private static final int LOCK_TIME_DURATION = 15;

    @Value("${jwt.expiration:3600000}") // Default 1 hour in milliseconds
    private long jwtExpirationInMs;

    // Login method delegates to authenticate
    public LoginResponse login(LoginRequest request) {
        return authenticate(request);
    }

    // Authentication logic for login
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou mot de passe incorrect"));

        // Check if user account is archived (disabled)
        if (user.isArchived()) {
            throw new InvalidCredentialsException("Compte désactivé");
        }

        // Check if account is locked due to failed attempts
        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Compte temporairement bloqué jusqu'à " + user.getAccountLockedUntil());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            // Increment failed attempts
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            // Lock account if max attempts reached
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION));
            }

            userRepository.save(user);
            throw new InvalidCredentialsException("Email ou mot de passe incorrect");
        }

        // Reset failed attempts and update last login on successful login
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate access token and refresh token
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user, null);

        logService.log(LogActionType.LOGIN_SUCCESS,
                "User logged in successfully: " + user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000) // Convert to seconds
                .build();
    }

    // Registration logic for new users
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email déjà utilisé");
        }
        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new MatriculeAlreadyExistsException("Matricule déjà utilisé");
        }

        // ✅ Force PENDING role for all new registrations (admin approval required)
        Role assignedRole = Role.PENDING;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(assignedRole)
                .matricule(request.getMatricule())
                .archived(false)
                .failedLoginAttempts(0)
                .accountLockedUntil(null)
                .build();

        userRepository.save(user);

        // Generate access token and refresh token
        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user, null);

        logService.log(LogActionType.CREATE_USER,
                "User registered successfully: " + user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000) // Convert to seconds
                .build();
    }

    /**
     * Refresh access token using refresh token
     * @param request RefreshTokenRequest containing the refresh token
     * @return RefreshTokenResponse with new access token and refresh token
     */
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // Validate refresh token and get user
        User user = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user.getEmail());

        // Generate new refresh token (token rotation for security)
        RefreshToken newRefreshToken = refreshTokenService.generateRefreshToken(user, null);

        // Revoke the old refresh token for security
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        logService.log(LogActionType.REFRESH_TOKEN_VALIDATED,
                "Token refreshed for user: " + user.getEmail());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000) // Convert to seconds
                .build();
    }

    /**
     * Logout user by revoking refresh token
     * @param refreshToken The refresh token to revoke
     */
    @Transactional
    public void logout(String refreshToken) {
        boolean revoked = refreshTokenService.revokeRefreshToken(refreshToken);

        if (revoked) {
            logService.log(LogActionType.REFRESH_TOKEN_REVOKED,
                    "User logged out successfully");
        }
    }

    /**
     * Logout user from all devices by revoking all refresh tokens
     * @param user The user to logout from all devices
     */
    @Transactional
    public void logoutAllDevices(User user) {
        int revokedCount = refreshTokenService.revokeAllRefreshTokensForUser(user);

        if (revokedCount > 0) {
            logService.log(LogActionType.REFRESH_TOKEN_REVOKED,
                    "User logged out from all devices: " + user.getEmail());
        }
    }

    /**
     * Validate if a refresh token is still valid
     * @param refreshToken The refresh token to validate
     * @return true if valid, false otherwise
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        return refreshTokenService.isRefreshTokenValid(refreshToken);
    }

    /**
     * Get user by refresh token
     * @param refreshToken The refresh token
     * @return Optional containing the user if token is valid
     */
    public java.util.Optional<User> getUserByRefreshToken(String refreshToken) {
        return refreshTokenService.getUserByRefreshToken(refreshToken);
    }
}