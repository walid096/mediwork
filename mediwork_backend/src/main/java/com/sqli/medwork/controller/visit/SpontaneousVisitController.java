package com.sqli.medwork.controller.visit;

import com.sqli.medwork.dto.request.SpontaneousVisitRequest;
import com.sqli.medwork.dto.response.SpontaneousVisitResponse;
import com.sqli.medwork.dto.request.UpdateSpontaneousVisitRequest;
import com.sqli.medwork.service.visit.SpontaneousVisitService;
import com.sqli.medwork.enums.SchedulingStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.sqli.medwork.enums.VisitType;
import com.sqli.medwork.dto.request.ConfirmSpontaneousVisitPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for managing spontaneous visit requests from collaborators
 */
@RestController
@RequestMapping("/api/spontaneous-visits")
@RequiredArgsConstructor
public class SpontaneousVisitController {

    private final SpontaneousVisitService spontaneousVisitService;

    /**
     * Create a new spontaneous visit request
     * Only collaborators can create requests
     */
    @PostMapping
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<SpontaneousVisitResponse> createSpontaneousVisit(
            @Valid @RequestBody SpontaneousVisitRequest request,
            Authentication authentication) {
        
        SpontaneousVisitResponse response = spontaneousVisitService.createSpontaneousVisit(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin/RH can cancel a spontaneous visit request (reject with optional reason)
     * Allowed only when request is not already scheduled.
     */
    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<Map<String, String>> adminCancelSpontaneousVisit(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String cancellationReason,
            Authentication authentication) {

        spontaneousVisitService.cancelSpontaneousVisitByAdmin(id, cancellationReason, authentication);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Demande de visite spontanée annulée par RH/ADMIN avec succès");
        response.put("id", id.toString());
        if (cancellationReason != null) {
            response.put("cancellationReason", cancellationReason);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all spontaneous visit requests for the authenticated collaborator
     * Supports filtering by status, date range
     * 
     * Query Parameters:
     * - status: Filter by scheduling status (PENDING, SCHEDULED, CANCELLED, NEEDS_RESCHEDULING)
     * - startDate: Filter requests from this date (ISO format: 2025-08-26T10:00:00)
     * - endDate: Filter requests until this date (ISO format: 2025-08-26T18:00:00)
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<List<SpontaneousVisitResponse>> getMySpontaneousVisits(
            @RequestParam(name = "status", required = false) SchedulingStatus status,
            @RequestParam(name = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        
        List<SpontaneousVisitResponse> visits;
        
        if (status != null || startDate != null || endDate != null) {
            visits = spontaneousVisitService.getMySpontaneousVisitsWithFilters(authentication, status, startDate, endDate);
        } else {
            visits = spontaneousVisitService.getMySpontaneousVisits(authentication);
        }
        
        return ResponseEntity.ok(visits);
    }

    /**
     * Get statistics for collaborator's spontaneous visit requests
     * Returns counts by status
     */
    @GetMapping("/my-requests/stats")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<Map<String, Object>> getMyRequestsStats(Authentication authentication) {
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pending", spontaneousVisitService.getMyRequestsCountByStatus(authentication, SchedulingStatus.PENDING));
        stats.put("scheduled", spontaneousVisitService.getMyRequestsCountByStatus(authentication, SchedulingStatus.SCHEDULED));
        stats.put("cancelled", spontaneousVisitService.getMyRequestsCountByStatus(authentication, SchedulingStatus.CANCELLED));
        stats.put("needsRescheduling", spontaneousVisitService.getMyRequestsCountByStatus(authentication, SchedulingStatus.NEEDS_RESCHEDULING));
        
        long total = spontaneousVisitService.getMySpontaneousVisits(authentication).size();
        stats.put("total", total);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get a specific spontaneous visit request by ID
     * Only the request owner can view it
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<SpontaneousVisitResponse> getSpontaneousVisit(
            @PathVariable Long id,
            Authentication authentication) {
        
        SpontaneousVisitResponse visit = spontaneousVisitService.getSpontaneousVisit(id, authentication);
        return ResponseEntity.ok(visit);
    }

    /**
     * Update a spontaneous visit request
     * Only the request owner can update it, and only if it's still pending
     * Supports partial updates - only provided fields will be updated
     * 
     * Request Body (all fields optional):
     * {
     *   "reason": "New reason for visit",
     *   "additionalNotes": "Updated notes",
     *   "preferredDateTime": "2025-08-30T14:00:00"
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<SpontaneousVisitResponse> updateSpontaneousVisit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSpontaneousVisitRequest request,
            Authentication authentication) {
        
        SpontaneousVisitResponse updatedVisit = spontaneousVisitService.updateSpontaneousVisit(id, request, authentication);
        return ResponseEntity.ok(updatedVisit);
    }

    /**
     * Partially update a spontaneous visit request using PATCH
     * Only the request owner can update it, and only if it's still pending
     * More flexible than PUT - allows updating individual fields
     * 
     * Query Parameters (all optional):
     * - reason: New reason for the visit
     * - notes: Updated additional notes
     * - datetime: New preferred date and time (ISO format: 2025-08-30T14:00:00)
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<SpontaneousVisitResponse> patchSpontaneousVisit(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @RequestParam(name = "notes", required = false) String additionalNotes,
            @RequestParam(name = "datetime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime preferredDateTime,
            Authentication authentication) {
        
        // Build update request from query parameters
        UpdateSpontaneousVisitRequest request = UpdateSpontaneousVisitRequest.builder()
                .reason(reason)
                .additionalNotes(additionalNotes)
                .preferredDateTime(preferredDateTime)
                .build();
        
        SpontaneousVisitResponse updatedVisit = spontaneousVisitService.updateSpontaneousVisit(id, request, authentication);
        return ResponseEntity.ok(updatedVisit);
    }

    /**
     * Cancel/Delete a spontaneous visit request
     * Only the request owner can cancel it, and only if it's still pending
     * 
     * Query Parameters (optional):
     * - reason: Reason for cancellation (for logging purposes)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<Map<String, String>> cancelSpontaneousVisit(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String cancellationReason,
            Authentication authentication) {
        
        spontaneousVisitService.cancelSpontaneousVisit(id, cancellationReason, authentication);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Demande de visite spontanée annulée avec succès");
        response.put("id", id.toString());
        if (cancellationReason != null) {
            response.put("cancellationReason", cancellationReason);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all spontaneous visit requests (for HR/Admin use)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<SpontaneousVisitResponse>> getAllSpontaneousVisits() {
        
        List<SpontaneousVisitResponse> visits = spontaneousVisitService.getAllSpontaneousVisits();
        return ResponseEntity.ok(visits);
    }

    /**
     * Confirm a spontaneous visit request by HR/Admin. Optionally change date, then auto-confirm.
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<SpontaneousVisitResponse> confirmSpontaneousVisit(
            @PathVariable Long id,
            @RequestParam(name = "dateTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(name = "doctorId", required = false) Long doctorId,
            @RequestParam(name = "visitType", required = false) VisitType visitType,
            @RequestBody(required = false) ConfirmSpontaneousVisitPayload body,
            Authentication authentication) {

        // Allow passing params either via query or JSON body
        LocalDateTime effectiveDateTime = dateTime;
        Long effectiveDoctorId = doctorId;
        VisitType effectiveVisitType = visitType;

        if (body != null) {
            if (effectiveDateTime == null) effectiveDateTime = body.getDateTime();
            if (effectiveDoctorId == null) effectiveDoctorId = body.getDoctorId();
            if (effectiveVisitType == null) effectiveVisitType = body.getVisitType();
        }

        if (effectiveVisitType == null) effectiveVisitType = VisitType.SPONTANEOUS;
        if (effectiveDoctorId == null) {
            throw new IllegalArgumentException("doctorId is required (query param or JSON body)");
        }

        SpontaneousVisitResponse response = spontaneousVisitService.confirmSpontaneousVisit(
                id, effectiveDateTime, effectiveDoctorId, effectiveVisitType, authentication);
        return ResponseEntity.ok(response);
    }

    /** Reject a spontaneous visit request (RH/ADMIN) */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<SpontaneousVisitResponse> rejectSpontaneousVisit(@PathVariable Long id) {
        SpontaneousVisitResponse response = spontaneousVisitService.rejectSpontaneousVisit(id);
        return ResponseEntity.ok(response);
    }
} 