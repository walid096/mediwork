package com.sqli.medwork.enums;

public enum LogActionType {
    // ==================== AUTHENTICATION ACTIONS ====================
    LOGIN_SUCCESS,             // User successfully logged in
    LOGIN_FAILURE,             // User failed to log in

    // ==================== USER MANAGEMENT ACTIONS ====================
    CREATE_USER,               // New user account created
    UPDATE_USER,               // User profile updated
    ARCHIVE_USER,              // User account archived
    RESTORE_USER,              // User account restored from archive

    // ==================== VISIT SCHEDULING ACTIONS ====================
    SCHEDULE_VISITE,           // HR scheduled a medical visit
    VALIDATE_VISITE,           // Visit validated by system
    REFUSE_VISITE,             // Visit refused/rejected
    GENERATE_REPORT,           // System report generated
    CANCEL_VISITE,             // Visit cancelled
    MARK_ABSENCE,              // Patient marked as absent
    SUBMIT_REQUEST_SPONTANEE,  // Spontaneous visit request submitted

    // ==================== TOKEN MANAGEMENT ACTIONS ====================
    REFRESH_TOKEN_ISSUED,      // When new refresh token is generated
    REFRESH_TOKEN_VALIDATED,   // When refresh token is successfully used
    REFRESH_TOKEN_REVOKED,     // When refresh token is revoked (logout)
    REFRESH_TOKEN_EXPIRED,     // When refresh token expires naturally

    // ==================== RECURRING SLOT ACTIONS ====================
    RECURRING_SLOT_CREATED,    // Doctor created recurring availability pattern
    RECURRING_SLOT_UPDATED,    // Doctor updated recurring availability pattern
    RECURRING_SLOT_DELETED     // Doctor deleted recurring availability pattern
}