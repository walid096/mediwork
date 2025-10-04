package com.sqli.medwork.controller.visit;

import com.sqli.medwork.dto.response.SlotResponse;
import com.sqli.medwork.dto.request.VisitRequest;
import com.sqli.medwork.dto.response.VisitResponse;
import com.sqli.medwork.dto.request.CreateVisitWithSlotRequest;
import com.sqli.medwork.dto.response.VisitWithSlotResponse;
import com.sqli.medwork.dto.response.SpontaneousVisitResponse;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.service.slot.SlotService;
import com.sqli.medwork.service.visit.VisitService;
import com.sqli.medwork.service.user.UserService;
import com.sqli.medwork.service.visit.SpontaneousVisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.sqli.medwork.enums.VisitStatus;
import java.time.LocalDateTime;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

/**
 * Controller for managing medical visits
 *
 * Core functionality:
 * - HR visit scheduling (US-RH-01)
 * - Doctor visit confirmation
 * - Visit management and status updates
 * - Available slots retrieval
 *
 * Security:
 * - HR role required for visit creation
 * - Doctor role required for visit confirmation
 * - Admin role for all operations
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;
    private final SlotService slotService;
    private final UserService userService;
    private final SpontaneousVisitService spontaneousVisitService;

    // ==================== CORE US-RH-01 FUNCTIONALITY ====================

    /**
     * Create a new medical visit (HR scheduling)
     *
     * Business Rules:
     * - Only HR users can create visits
     * - Slot must be available
     * - No conflicts allowed
     * - Automatic slot locking
     *
     * @param request Visit creation request
     * @param userDetails Authenticated user (must be HR)
     * @return Created visit response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<VisitResponse> createVisit(
            @Valid @RequestBody VisitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HR user {} creating visit: {}", userDetails.getUsername(), request.getRequestSummary());

        try {
            User hrUser = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.createVisit(request, hrUser);

            log.info("Visit created successfully: ID={}", visit.getId());
            return ResponseEntity.ok(visit);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid visit request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new medical visit with a new slot (HR scheduling)
     *
     * Business Rules:
     * - Only HR users can create visits with slots
     * - Creates both slot and visit in one transaction
     * - Slot must not conflict with existing slots
     * - Start time must be in the future
     * - Duration must be reasonable (15 min to 2 hours)
     * - Automatic slot creation and visit scheduling
     *
     * @param request Visit with slot creation request
     * @param userDetails Authenticated user (must be HR)
     * @return Created visit with slot response
     */
    @PostMapping("/with-slot")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<VisitWithSlotResponse> createVisitWithSlot(
            @Valid @RequestBody CreateVisitWithSlotRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("HR user {} creating visit with slot: {}", userDetails.getUsername(), request.getRequestSummary());

        try {
            // Validate request business rules
            if (!request.isStartTimeInFuture()) {
                log.warn("Start time must be in the future: {}", request.getStartTime());
                return ResponseEntity.badRequest().build();
            }

            if (!request.isTimeRangeValid()) {
                log.warn("Invalid time range: start={}, end={}", request.getStartTime(), request.getEndTime());
                return ResponseEntity.badRequest().build();
            }

            if (!request.isDurationReasonable()) {
                log.warn("Duration not reasonable: {} minutes", request.getDurationInMinutes());
                return ResponseEntity.badRequest().build();
            }

            User hrUser = userService.getUserByEmail(userDetails.getUsername());
            VisitWithSlotResponse result = visitService.createVisitWithSlot(request, hrUser);

            log.info("Visit with slot created successfully: Visit ID={}, Slot ID={}",
                    result.getVisit().getId(), result.getSlot().getId());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid visit with slot request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating visit with slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available slots for a specific doctor
     *
     * Used by HR to populate slot dropdown when scheduling visits
     * Returns only AVAILABLE future slots
     *
     * @param doctorId Doctor ID to get slots for
     * @return List of available slots
     */
    @GetMapping("/available-slots/{doctorId}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(@PathVariable Long doctorId) {

        log.info("Getting available slots for doctor: {}", doctorId);

        try {
            List<SlotResponse> slots = slotService.getAvailableSlotsForDoctor(doctorId);
            log.info("Found {} available slots for doctor: {}", slots.size(), doctorId);
            return ResponseEntity.ok(slots);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid doctor ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting available slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get existing visits for a doctor on a specific date (for conflict checking)
     *
     * Used by RH to check for conflicts when selecting time slots
     * Returns visits that could conflict with new scheduling
     *
     * @param doctorId Doctor ID
     * @param date Date in YYYY-MM-DD format
     * @return List of existing visits on that date
     */
    @GetMapping("/doctor/{doctorId}/date/{date}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<VisitResponse>> getVisitsByDoctorAndDate(
            @PathVariable Long doctorId,
            @PathVariable String date) {

        log.info("Getting visits for doctor {} on date: {}", doctorId, date);

        try {
            List<VisitResponse> visits = visitService.getVisitsByDoctorAndDate(doctorId, date);
            log.info("Found {} visits for doctor {} on date {}", visits.size(), doctorId, date);
            return ResponseEntity.ok(visits);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting visits by doctor and date: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Confirm a visit (Doctor acceptance)
     *
     * Changes visit status from PENDING_DOCTOR_CONFIRMATION to SCHEDULED
     * Changes slot status from TEMPORARILY_LOCKED to CONFIRMED
     *
     * @param visitId Visit ID to confirm
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated visit response
     */
    @PutMapping("/{visitId}/confirm")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<VisitResponse> confirmVisit(
            @PathVariable Long visitId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} confirming visit: {}", userDetails.getUsername(), visitId);

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.confirmVisit(visitId, doctorUser);

            log.info("Visit confirmed successfully: ID={}", visitId);
            return ResponseEntity.ok(visit);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid visit ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Visit cannot be confirmed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error confirming visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel a visit
     *
     * Releases the slot back to AVAILABLE status
     * Updates visit status to CANCELLED
     *
     * @param visitId Visit ID to cancel
     * @param userDetails Authenticated user
     * @return Updated visit response
     */
    @PutMapping("/{visitId}/cancel")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<VisitResponse> cancelVisit(
            @PathVariable Long visitId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} cancelling visit: {}", userDetails.getUsername(), visitId);

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.cancelVisit(visitId, user);

            log.info("Visit cancelled successfully: ID={}", visitId);
            return ResponseEntity.ok(visit);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid visit ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Visit cannot be cancelled: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error cancelling visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== QUERY ENDPOINTS ====================

    /**
     * Get visits for the authenticated user
     *
     * Returns different data based on user role:
     * - RH: Visits created by them
     * - DOCTOR: Visits assigned to them
     * - COLLABORATOR: Visits for them
     * - ADMIN: All visits
     *
     * @param userDetails Authenticated user
     * @return List of visits relevant to the user
     */
    @GetMapping("/my-visits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VisitResponse>> getMyVisits(@AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting visits for user: {}", userDetails.getUsername());

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            List<VisitResponse> visits;

            switch (user.getRole()) {
                case RH:
                    visits = visitService.getVisitsByCreatedBy(user.getId());
                    break;
                case DOCTOR:
                    visits = visitService.getDoctorVisits(user.getId());
                    break;
                case COLLABORATOR:
                    visits = visitService.getCollaboratorVisits(user.getId());
                    break;
                case ADMIN:
                    visits = visitService.getAllVisits();
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            log.info("Found {} visits for user: {}", visits.size(), userDetails.getUsername());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error getting user visits: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get visits by status (Admin/RH use)
     *
     * @param status Visit status to filter by
     * @return List of visits with the specified status
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<VisitResponse>> getVisitsByStatus(@PathVariable String status) {

        log.info("Getting visits by status: {}", status);

        try {
            VisitStatus visitStatus = VisitStatus.valueOf(status.toUpperCase());
            List<VisitResponse> visits = visitService.getVisitsByStatus(visitStatus);

            log.info("Found {} visits with status: {}", visits.size(), status);
            return ResponseEntity.ok(visits);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting visits by status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get visit by ID
     *
     * @param visitId Visit ID
     * @param userDetails Authenticated user
     * @return Visit details
     */
    @GetMapping("/{visitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VisitResponse> getVisitById(
            @PathVariable Long visitId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("User {} getting visit: {}", userDetails.getUsername(), visitId);

        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            VisitResponse visit = visitService.getVisitById(visitId, user);

            return ResponseEntity.ok(visit);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid visit ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Access denied: {}", e.getMessage());
            // ✅ FIXED: Use status(403) instead of forbidden()
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error getting visit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all visits (Admin only)
     *
     * @return List of all visits in the system
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitResponse>> getAllVisits() {

        log.info("Admin getting all visits");

        try {
            List<VisitResponse> visits = visitService.getAllVisits();
            log.info("Found {} total visits", visits.size());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error getting all visits: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get visits within a date range (Admin/RH reporting)
     *
     * @param startDate Start date (ISO format)
     * @param endDate End date (ISO format)
     * @return List of visits in the date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<VisitResponse>> getVisitsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Getting visits from {} to {}", startDate, endDate);

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<VisitResponse> visits = visitService.getVisitsByDateRange(start, end);
            log.info("Found {} visits in date range", visits.size());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error getting visits by date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== COLLABORATOR ENDPOINTS ====================

    /**
     * Get collaborator's visit history
     *
     * Used by collaborators to:
     * - View their visit history
     * - Track visit statuses
     * - Monitor their medical appointments
     *
     * @param userDetails Authenticated collaborator
     * @return List of collaborator's visits
     */
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<List<VisitResponse>> getMyVisitHistory(@AuthenticationPrincipal UserDetails userDetails) {

        log.info("Collaborator {} requesting visit history", userDetails.getUsername());

        try {
            User collaborator = userService.getUserByEmail(userDetails.getUsername());
            List<VisitResponse> visits = visitService.getVisitsByCollaborator(collaborator.getId());

            log.info("Found {} visits for collaborator {}", visits.size(), userDetails.getUsername());
            return ResponseEntity.ok(visits);

        } catch (Exception e) {
            log.error("Error retrieving visit history for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get collaborator's spontaneous visit requests
     *
     * Used by collaborators to:
     * - View their spontaneous visit requests
     * - Track request statuses
     * - Monitor pending requests
     *
     * @param userDetails Authenticated collaborator
     * @return List of collaborator's spontaneous visit requests
     */
    @GetMapping("/my-spontaneous-requests")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<List<SpontaneousVisitResponse>> getMySpontaneousRequests(@AuthenticationPrincipal UserDetails userDetails) {

        log.info("Collaborator {} requesting spontaneous visit requests", userDetails.getUsername());

        try {
            List<SpontaneousVisitResponse> requests = spontaneousVisitService.getMySpontaneousVisits(
                    new org.springframework.security.core.Authentication() {
                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                            return userDetails.getAuthorities();
                        }

                        @Override
                        public Object getCredentials() {
                            return null;
                        }

                        @Override
                        public Object getDetails() {
                            return null;
                        }

                        @Override
                        public Object getPrincipal() {
                            return userDetails;
                        }

                        @Override
                        public boolean isAuthenticated() {
                            return true;
                        }

                        @Override
                        public String getName() {
                            return userDetails.getUsername();
                        }

                        @Override
                        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                            // Not needed for this use case
                        }
                    });

            log.info("Found {} spontaneous visit requests for collaborator {}", requests.size(), userDetails.getUsername());
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            log.error("Error retrieving spontaneous visit requests for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}