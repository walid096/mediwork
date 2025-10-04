package com.sqli.medwork.enums;

/**
 * Enum for preferred time slots in spontaneous visit requests
 */
public enum TimeSlot {
    MORNING("Matin (8h-12h)"),
    AFTERNOON("Après-midi (13h-17h)"),
    EVENING("Soirée (17h-20h)");

    private final String description;

    TimeSlot(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 