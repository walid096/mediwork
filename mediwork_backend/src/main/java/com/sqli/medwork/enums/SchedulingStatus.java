package com.sqli.medwork.enums;

/**
 * Enum for scheduling status of spontaneous visit requests
 */
public enum SchedulingStatus {
    PENDING("En attente de planification"),
    SCHEDULED("Planifiée"),
    NEEDS_RESCHEDULING("Nécessite une replanification"),
    CANCELLED("Annulée");

    private final String description;

    SchedulingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 