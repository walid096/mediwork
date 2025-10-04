package com.sqli.medwork.controller.visit;

import com.sqli.medwork.dto.response.VisitResponse;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.VisitStatus;
import com.sqli.medwork.service.visit.VisitService;
import com.sqli.medwork.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for doctor-specific visit operations
 *
 * Core functionality:
 * - View pending visit confirmations
 * - View confirmed schedule
 * - Confirm/reject visits
 * - Update visit status (in-progress, completed)
 *
 * Security:
 * - Only DOCTOR and ADMIN roles can access
 * - Doctors can only manage their own visits
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor/visits")
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class DoctorVisitController {

    private final VisitService visitService;
    private final UserService userService;

    // ==================== VIEW OPERATIONS ====================

    /**
     * Get visits pending doctor confirmation
     *
     * Returns visits with PENDING_DOCTOR_CONFIRMATION status
     * for the authenticated doctor
     *
     * @param userDetails Authenticated user (must be doctor)
     * @return List of visits waiting for confirmation
     */
    @GetMapping("/pending-confirmations")
    public ResponseEntity<List<VisitResponse>> getPendingConfirmations(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} getting pending confirmations", userDetails.getUsername());

        try {
            User doctor = userService.getUserByEmail(userDetails.getUsername());
            List<VisitResponse> visits = visitService.getPendingConfirmationsForDoctor(doctor.getId());

            log.info("Found {} pending confirmations for doctor: {}", visits.size(), userDetails.getUsername());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error getting pending confirmations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get doctor's confirmed schedule
     *
     * Returns visits with SCHEDULED status for the authenticated doctor
     *
     * @param userDetails Authenticated user (must be doctor)
     * @return List of confirmed visits
     */
    @GetMapping("/my-schedule")
    public ResponseEntity<List<VisitResponse>> getMySchedule(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} getting their schedule", userDetails.getUsername());

        try {
            User doctor = userService.getUserByEmail(userDetails.getUsername());
            List<VisitResponse> visits = visitService.getConfirmedScheduleForDoctor(doctor.getId());

            log.info("Found {} scheduled visits for doctor: {}", visits.size(), userDetails.getUsername());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error getting doctor schedule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ACTION OPERATIONS ====================

    /**
     * Confirm a visit (Doctor use)
     *
     * Changes visit status from PENDING_DOCTOR_CONFIRMATION to SCHEDULED
     * Changes slot status from TEMPORARILY_LOCKED to CONFIRMED
     *
     * @param visitId Visit ID to confirm
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated visit response
     */
    @PutMapping("/{visitId}/confirm")
    public ResponseEntity<VisitResponse> confirmVisit(
            @PathVariable Long visitId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} confirming visit: {}", userDetails.getUsername(), visitId);

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.confirmVisit(visitId, doctorUser);

            log.info("Visit confirmed successfully: ID={}", visitId);
            return ResponseEntity.ok(visit);

        } catch (RuntimeException e) {  // ✅ FIXED: Changed to RuntimeException
            log.warn("Visit confirmation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error confirming visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reject a visit (Doctor use)
     *
     * Changes visit status to CANCELLED
     * Releases the slot back to AVAILABLE
     *
     * @param visitId Visit ID to reject
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated visit response
     */
    @PutMapping("/{visitId}/reject")
    public ResponseEntity<VisitResponse> rejectVisit(
            @PathVariable Long visitId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} rejecting visit: {}", userDetails.getUsername(), visitId);

        try {
            User doctor = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.rejectVisit(visitId, doctor);

            log.info("Visit rejected successfully: ID={}", visitId);
            return ResponseEntity.ok(visit);

        } catch (RuntimeException e) {  // ✅ FIXED: Changed to RuntimeException
            log.warn("Visit rejection failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rejecting visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update visit status (Doctor use)
     *
     * Allows doctors to update visit status (IN_PROGRESS, COMPLETED)
     *
     * @param visitId Visit ID to update
     * @param status New status
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated visit response
     */
    @PutMapping("/{visitId}/status")
    public ResponseEntity<VisitResponse> updateVisitStatus(
            @PathVariable Long visitId,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} updating visit {} status to: {}", userDetails.getUsername(), visitId, status);

        try {
            User doctor = userService.getUserByEmail(userDetails.getUsername());
            VisitStatus visitStatus = VisitStatus.valueOf(status.toUpperCase());
            VisitResponse visit = visitService.updateVisitStatus(visitId, visitStatus, doctor);

            log.info("Visit status updated successfully: ID={}, Status={}", visitId, status);
            return ResponseEntity.ok(visit);

        } catch (RuntimeException e) {  // ✅ FIXED: Changed to RuntimeException
            log.warn("Status update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating visit status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}