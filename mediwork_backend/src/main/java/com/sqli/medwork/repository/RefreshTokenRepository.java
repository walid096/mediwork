package com.sqli.medwork.repository;

import com.sqli.medwork.entity.RefreshToken;
import com.sqli.medwork.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find refresh token by token string
    Optional<RefreshToken> findByToken(String token);

    // Find all active (not revoked) refresh tokens for a user
    List<RefreshToken> findByUserAndRevokedFalse(User user);

    // Delete all tokens expired before a specific date (cleanup)
    void deleteByExpiryDateBefore(LocalDateTime date);

    // Check if a non-revoked token exists
    boolean existsByTokenAndRevokedFalse(String token);

    // Revoke a specific token (mark as revoked)
    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.revoked = true where rt.token = :token")
    int revokeByToken(@Param("token") String token);

    // Revoke all tokens for a given user (e.g., logout all sessions)
    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.revoked = true where rt.user = :user")
    int revokeAllByUser(@Param("user") User user);
}
