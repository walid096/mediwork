package com.sqli.medwork.repository;

import com.sqli.medwork.entity.SpontaneousVisitDetails;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.SchedulingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing spontaneous visit details
 */
@Repository
public interface SpontaneousVisitDetailsRepository extends JpaRepository<SpontaneousVisitDetails, Long> {

    /**
     * Find all details created by collaborator
     */
    List<SpontaneousVisitDetails> findByCollaborator(User collaborator);

    /**
     * Find details by collaborator and status
     */
    List<SpontaneousVisitDetails> findByCollaboratorAndSchedulingStatus(User collaborator, SchedulingStatus status);

    /**
     * Find details by collaborator and status, ordered by creation date
     */
    List<SpontaneousVisitDetails> findByCollaboratorAndSchedulingStatusOrderByCreatedAtDesc(User collaborator, SchedulingStatus status);

    /**
     * Find details by collaborator within date range
     */
    @Query("SELECT svd FROM SpontaneousVisitDetails svd WHERE svd.collaborator = :collaborator " +
           "AND svd.preferredDateTime BETWEEN :startDate AND :endDate " +
           "ORDER BY svd.preferredDateTime ASC")
    List<SpontaneousVisitDetails> findByCollaboratorAndDateRange(
            @Param("collaborator") User collaborator,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find details by collaborator with status and date range
     */
    @Query("SELECT svd FROM SpontaneousVisitDetails svd WHERE svd.collaborator = :collaborator " +
           "AND (:status IS NULL OR svd.schedulingStatus = :status) " +
           "AND (:startDate IS NULL OR svd.preferredDateTime >= :startDate) " +
           "AND (:endDate IS NULL OR svd.preferredDateTime <= :endDate) " +
           "ORDER BY svd.preferredDateTime DESC")
    List<SpontaneousVisitDetails> findByCollaboratorWithFilters(
            @Param("collaborator") User collaborator,
            @Param("status") SchedulingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count requests by collaborator and status
     */
    long countByCollaboratorAndSchedulingStatus(User collaborator, SchedulingStatus status);
}