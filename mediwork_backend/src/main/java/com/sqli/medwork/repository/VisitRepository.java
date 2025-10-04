package com.sqli.medwork.repository;

import com.sqli.medwork.entity.Visit;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.VisitStatus;
import com.sqli.medwork.enums.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing medical visits
 *
 * Provides data access for:
 * - HR visit scheduling and management
 * - Doctor visit confirmation and tracking
 * - Visit status lifecycle management
 * - Reporting and analytics
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // ==================== CORE QUERIES ====================

    /** Find visits by collaborator (employee) */
    List<Visit> findByCollaboratorId(Long collaboratorId);

    /** Find visits by doctor */
    List<Visit> findByDoctorId(Long doctorId);

    /** Find visits by slot */
    Optional<Visit> findBySlotId(Long slotId);

    /** Find visits created by specific HR user */
    List<Visit> findByCreatedById(Long createdById);

    /** Find visits by status */
    List<Visit> findByStatus(VisitStatus status);

    /** Find visits by collaborator and status */
    List<Visit> findByCollaboratorIdAndStatus(Long collaboratorId, VisitStatus status);

    /** Find confirmed visits for a doctor */
    List<Visit> findByDoctorIdAndStatus(Long doctorId, VisitStatus status);

    // ==================== TIME-BASED QUERIES ====================

    /** Find visits within a date range */
    List<Visit> findBySlotStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /** Find visits for a specific date */
    @Query("SELECT v FROM Visit v WHERE DATE(v.slot.startTime) = DATE(:date)")
    List<Visit> findByDate(@Param("date") LocalDateTime date);

    /** Find upcoming visits for a user */
    @Query("SELECT v FROM Visit v WHERE v.slot.startTime > :now ORDER BY v.slot.startTime")
    List<Visit> findUpcomingVisits(@Param("now") LocalDateTime now);

    // ✅ FIXED: Find visits by doctor and start time between two dates
    /**
     * Find visits by doctor and start time between two dates
     *
     * Used by RH to check for conflicts when selecting time slots
     * Returns visits that could conflict with new scheduling
     *
     * @param doctor Doctor to find visits for
     * @param startTime Start of the date range
     * @param endTime End of the date range
     * @param statuses List of visit statuses to filter by
     * @return List of visits in the date range
     */
    @Query("SELECT v FROM Visit v WHERE v.doctor = :doctor " +
            "AND v.slot.startTime BETWEEN :startTime AND :endTime " +
            "AND v.status IN (:statuses) " +
            "ORDER BY v.slot.startTime")
    List<Visit> findByDoctorAndStartTimeBetween(
            @Param("doctor") User doctor,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<VisitStatus> statuses
    );

    // ==================== BUSINESS RULES ====================

    /** Check if collaborator has conflicting visits */
    @Query("SELECT COUNT(v) > 0 FROM Visit v WHERE v.collaborator.id = :collaboratorId " +
            "AND v.slot.startTime < :endTime AND v.slot.endTime > :startTime " +
            "AND v.status IN (:statuses)")
    boolean hasConflictingVisits(
            @Param("collaboratorId") Long collaboratorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<VisitStatus> statuses
    );

    /** Check if doctor has conflicting visits */
    @Query("SELECT COUNT(v) > 0 FROM Visit v WHERE v.doctor.id = :doctorId " +
            "AND v.slot.startTime < :endTime AND v.slot.endTime > :startTime " +
            "AND v.status IN (:statuses)")
    boolean doctorHasConflictingVisits(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<VisitStatus> statuses
    );

    // ==================== REPORTING ====================

    /** Count visits by type for a period */
    @Query("SELECT v.visitType, COUNT(v) FROM Visit v " +
            "WHERE v.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY v.visitType")
    List<Object[]> countVisitsByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /** Count visits by status for a doctor */
    long countByDoctorIdAndStatus(Long doctorId, VisitStatus status);

    /** Find visits created within a time period */
    List<Visit> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== WORKFLOW MANAGEMENT ====================

    /** Find visits that need attention (pending confirmation) */
    List<Visit> findByStatusAndSlotStartTimeAfter(
            VisitStatus status, LocalDateTime cutoffTime
    );

    /** Find expired pending confirmations */
    @Query("SELECT v FROM Visit v WHERE v.status = :status " +
            "AND v.slot.startTime < :cutoffTime")
    List<Visit> findExpiredPendingConfirmations(
            @Param("status") VisitStatus status,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    // ==================== SPONTANEOUS VISIT QUERIES ====================

    /** Find visits by collaborator and visit type */
    List<Visit> findByCollaboratorAndVisitType(com.sqli.medwork.entity.User collaborator, com.sqli.medwork.enums.VisitType visitType);

    /** Find visits by visit type */
    List<Visit> findByVisitType(com.sqli.medwork.enums.VisitType visitType);

    /** Find spontaneous visits by status */
    List<Visit> findByVisitTypeAndStatus(com.sqli.medwork.enums.VisitType visitType, VisitStatus status);
}