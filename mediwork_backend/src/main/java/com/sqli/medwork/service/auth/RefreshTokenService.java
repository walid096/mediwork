package com.sqli.medwork.service.auth;

import com.sqli.medwork.entity.RefreshToken;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.exception.InvalidRefreshTokenException;
import com.sqli.medwork.exception.RefreshTokenExpiredException;
import com.sqli.medwork.exception.RefreshTokenNotFoundException;
import com.sqli.medwork.repository.RefreshTokenRepository;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.service.common.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final LogService logService;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token.expiration:604800}") // 7 days in seconds
    private long refreshTokenExpiration;

    @Value("${jwt.refresh-token.length:64}")
    private int refreshTokenLength;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generate a new refresh token for a user.
     * Optionally revoke old tokens if single session is desired.
     */
    @Transactional
    public RefreshToken generateRefreshToken(User user, String deviceInfo) {
        // Uncomment below line to revoke all previous tokens for single session apps
        // refreshTokenRepository.revokeAllByUser(user);

        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiration);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .revoked(false)
                .deviceInfo(deviceInfo)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        logService.log(LogActionType.REFRESH_TOKEN_ISSUED,
                "Refresh token generated for user: " + user.getEmail());

        log.info("Generated refresh token for user: {}", user.getEmail());

        return savedToken;
    }

    /**
     * Validate a refresh token and return the associated user.
     * Throws exceptions if invalid or expired.
     */
    @Transactional(readOnly = true)
    public User validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        logService.log(LogActionType.REFRESH_TOKEN_VALIDATED,
                "Refresh token validated for user: " + refreshToken.getUser().getEmail());

        return refreshToken.getUser();
    }

    /**
     * Revoke a specific refresh token.
     * @return true if revoked, false if token not found.
     */
    @Transactional
    public boolean revokeRefreshToken(String token) {
        int updatedRows = refreshTokenRepository.revokeByToken(token);

        if (updatedRows > 0) {
            logService.log(LogActionType.REFRESH_TOKEN_REVOKED,
                    "Refresh token revoked: " + token);
            log.info("Revoked refresh token: {}", token);
            return true;
        }
        return false;
    }

    /**
     * Revoke all refresh tokens for a user.
     * @return number of tokens revoked.
     */
    @Transactional
    public int revokeAllRefreshTokensForUser(User user) {
        int revokedCount = refreshTokenRepository.revokeAllByUser(user);

        if (revokedCount > 0) {
            logService.log(LogActionType.REFRESH_TOKEN_REVOKED,
                    "All refresh tokens revoked for user: " + user.getEmail());
            log.info("Revoked {} refresh tokens for user: {}", revokedCount, user.getEmail());
        }

        return revokedCount;
    }

    /**
     * Get all active (not revoked) refresh tokens for a user.
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveTokensForUser(User user) {
        return refreshTokenRepository.findByUserAndRevokedFalse(user);
    }

    /**
     * Check if a refresh token exists and is valid (not revoked and not expired).
     */
    @Transactional(readOnly = true)
    public boolean isRefreshTokenValid(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        return refreshToken.isPresent() &&
                !refreshToken.get().isExpired() &&
                !refreshToken.get().isRevoked();
    }

    /**
     * Scheduled cleanup of expired refresh tokens (runs daily at 2 AM).
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now();
        refreshTokenRepository.deleteByExpiryDateBefore(cutoffDate);

        log.info("Cleanup completed: removed expired refresh tokens");
    }

    /**
     * Generate a secure random token encoded in URL-safe Base64 without padding.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[refreshTokenLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Get refresh token entity by token string.
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> getRefreshTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Update device info for a refresh token.
     * @return true if update successful, false otherwise.
     */
    @Transactional
    public boolean updateDeviceInfo(String token, String deviceInfo) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isPresent()) {
            RefreshToken tokenEntity = refreshToken.get();
            tokenEntity.setDeviceInfo(deviceInfo);
            refreshTokenRepository.save(tokenEntity);
            return true;
        }
        return false;
    }

    /**
     * Get user by refresh token if token is active.
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isPresent() && refreshToken.get().isActive()) {
            return Optional.of(refreshToken.get().getUser());
        }

        return Optional.empty();
    }
}
