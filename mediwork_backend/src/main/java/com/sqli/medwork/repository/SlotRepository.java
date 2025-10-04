package com.sqli.medwork.repository;

import com.sqli.medwork.entity.Slot;
import com.sqli.medwork.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing medical appointment time slots
 *
 * Provides data access for:
 * - HR visit scheduling (US1 core functionality)
 * - Slot status management
 * - Availability validation and double-booking prevention
 * - Doctor schedule management
 * - Admin reporting
 */
@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    // ==================== CORE US1 FUNCTIONALITY ====================

    /** Find available future slots for a specific doctor - HR dropdown population */
    List<Slot> findByDoctorIdAndStatusAndStartTimeAfter(Long doctorId, SlotStatus status, LocalDateTime now);

    /** Find slot by ID and specific status - validation for visit creation */
    Optional<Slot> findByIdAndStatus(Long id, SlotStatus status);

    /** Find slots by doctor and status - doctor dashboard */
    List<Slot> findByDoctorIdAndStatus(Long doctorId, SlotStatus status);

    // ==================== BUSINESS RULE ENFORCEMENT ====================

    /** Check for overlapping slots to prevent double-booking */
    boolean existsByDoctorIdAndStartTimeBetweenAndStatusIn(
            Long doctorId, LocalDateTime start, LocalDateTime end, List<SlotStatus> statuses
    );

    // ✅ ADDED: Find conflicting slots for a doctor in a time range
    /**
     * Find slots that conflict with the given time range for a doctor
     * Used to prevent double-booking when creating new slots
     * 
     * @param doctor Doctor to check conflicts for
     * @param startTime Start time of new slot
     * @param endTime End time of new slot
     * @return List of conflicting slots
     */
    @Query("SELECT s FROM Slot s WHERE s.doctor = :doctor " +
            "AND s.status IN ('AVAILABLE', 'CONFIRMED', 'TEMPORARILY_LOCKED') " +
            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime) " +
            "ORDER BY s.startTime")
    List<Slot> findConflictingSlots(
            @Param("doctor") com.sqli.medwork.entity.User doctor,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ==================== DOCTOR-SPECIFIC QUERIES ====================

    /** Find all slots for a doctor */
    List<Slot> findByDoctorId(Long doctorId);

    /** Find all slots for a doctor within a date range */
    @Query("SELECT s FROM Slot s WHERE s.doctor.id = :doctorId " +
            "AND DATE(s.startTime) BETWEEN DATE(:startDate) AND DATE(:endDate) " +
            "ORDER BY s.startTime")
    List<Slot> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /** Find expired TEMPORARILY_LOCKED slots - cleanup task */
    List<Slot> findByStatusAndStartTimeBefore(SlotStatus status, LocalDateTime cutoffTime);

    // ==================== ADMIN AND REPORTING ====================

    /** Count slots by status for a doctor */
    long countByDoctorIdAndStatus(Long doctorId, SlotStatus status);

    /** Find slots created within a time period */
    List<Slot> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}
