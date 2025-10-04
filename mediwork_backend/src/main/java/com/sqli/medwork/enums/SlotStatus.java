package com.sqli.medwork.enums;

/**
 * SlotStatus: Manages medical appointment time slot lifecycle
 * Prevents double-booking through controlled status transitions
 */
public enum SlotStatus {

    /** Doctor has marked this time as available for appointments */
    AVAILABLE,

    /** HR has booked this slot, waiting for doctor confirmation, expires after 2 hours if doctor doesn't respond */
    TEMPORARILY_LOCKED,

    /** Permanently booked - doctor has accepted, cannot be changed without admin override */
    CONFIRMED,

    /** Doctor has marked as unavailable (leave, busy, etc.) Or visit completed */
    UNAVAILABLE
}