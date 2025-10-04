package com.sqli.medwork.enums;

public enum VisitStatus {
    // ==================== VISIT LIFE CYCLE STATUSES ====================
    PENDING_DOCTOR_CONFIRMATION,  // HR scheduled, waiting for doctor
    SCHEDULED,                     // Doctor confirmed, visit is set
    IN_PROGRESS,                   // Visit is happening now
    COMPLETED,                     // Visit finished successfully
    CANCELLED,                     // Visit was cancelled

    // ==================== VISIT ACTION STATUSES ====================
    VISIT_CONFIRMED,               // Doctor confirmed the visit
    VISIT_REJECTED,                // Doctor rejected the visit
    VISIT_STATUS_UPDATED           // Visit status was updated
}