package com.sqli.medwork.enums;

public enum VisitType {
    // ==================== EMPLOYMENT LIFE CYCLE VISITS ====================
    HIRING,               // Initial hiring medical visit
    PERIODIC,             // Periodic/annual follow-up visit
    RETURN_TO_WORK,       // Return after sick leave or accident
    PRE_RETURN,           // Pre-return visit before coming back from long absence
    JOB_CHANGE,           // Job change or change in working conditions

    // ==================== REQUEST-BASED VISITS ====================
    SPONTANEOUS,          // Spontaneous request from the employee
    MEDICAL_FOLLOW_UP,    // Specific follow-up (fitness, restrictions)
    EXCEPTIONAL_VISIT     // Other occasional visits (prevention, control)
}