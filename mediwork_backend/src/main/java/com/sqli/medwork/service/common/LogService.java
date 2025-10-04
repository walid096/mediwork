package com.sqli.medwork.service.common;

import com.sqli.medwork.entity.Log;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for logging user actions and system events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    /**
     * Log an action with proper error handling and fallback
     */
    public void log(LogActionType actionType, String description) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String email = "SYSTEM"; // Default for system operations
            String role = "SYSTEM";

            // Try to get authenticated user info if available
            if (auth != null && auth.isAuthenticated()) {
                email = auth.getName();
                role = auth.getAuthorities().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("UNKNOWN")
                        .replace("ROLE_", "");
            }

            // Create and save the log entry
            Log logEntry = Log.builder()
                    .performedBy(email)
                    .actionType(actionType)
                    .role(role)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .build();

            logRepository.save(logEntry);

        } catch (Exception e) {
            // Fallback logging if database logging fails
            log.error("Failed to log action to database: {} - {}. Error: {}",
                    actionType, description, e.getMessage());
        }
    }
}