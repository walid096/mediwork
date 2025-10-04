package com.sqli.medwork.controller.slot;

import com.sqli.medwork.dto.response.SlotResponse;
import com.sqli.medwork.dto.request.CreateSlotRequest;
import com.sqli.medwork.dto.request.UpdateSlotStatusRequest;
import com.sqli.medwork.dto.response.CleanupResponse;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.SlotStatus;
import com.sqli.medwork.service.slot.SlotService;
import com.sqli.medwork.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for managing medical appointment time slots
 *
 * Core functionality:
 * - Doctor slot management (create, update, delete)
 * - HR slot availability queries
 * - Slot status lifecycle management
 * - Admin reporting and maintenance
 *
 * Security:
 * - Doctors can manage their own slots
 * - HR can view available slots for scheduling
 * - Admin has full access to all slots
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
public class SlotController {

    private final SlotService slotService;
    private final UserService userService;

    // ==================== DOCTOR SLOT MANAGEMENT ====================

    /**
     * Create a new time slot (Doctor use)
     *
     * Business Rules:
     * - Only doctors can create slots for themselves
     * - Slots must be in the future
     * - No overlapping slots allowed
     * - Initial status is AVAILABLE
     *
     * @param request Slot creation request
     * @param userDetails Authenticated user (must be doctor)
     * @return Created slot response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<SlotResponse> createSlot(
            @Valid @RequestBody CreateSlotRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} creating slot: {} to {}",
                userDetails.getUsername(), request.getStartTime(), request.getEndTime());

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            SlotResponse slot = slotService.createSlot(request, doctorUser);

            log.info("Slot created successfully: ID={}", slot.getId());
            return ResponseEntity.ok(slot);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid slot request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update slot status (Doctor use)
     *
     * @param slotId Slot ID to update
     * @param request Status update request
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated slot response
     */
    @PutMapping("/{slotId}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<SlotResponse> updateSlotStatus(
            @PathVariable Long slotId,
            @Valid @RequestBody UpdateSlotStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} updating slot {} status to: {}",
                userDetails.getUsername(), slotId, request.getStatus());

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            SlotResponse slot = slotService.updateSlotStatus(slotId, request.getStatus(), doctorUser);

            log.info("Slot status updated successfully: ID={}, Status={}", slotId, slot.getStatus());
            return ResponseEntity.ok(slot);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid slot ID or status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Slot cannot be updated: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating slot status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a slot (Doctor use)
     *
     * @param slotId Slot ID to delete
     * @param userDetails Authenticated user (must be doctor)
     * @return Success response
     */
    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long slotId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} deleting slot: {}", userDetails.getUsername(), slotId);

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            slotService.deleteSlot(slotId, doctorUser);

            log.info("Slot deleted successfully: ID={}", slotId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Invalid slot ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Slot cannot be deleted: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== SLOT QUERY ENDPOINTS ====================

    /**
     * Get available slots for a specific doctor (HR use)
     *
     * Used by HR to populate slot dropdown when scheduling visits
     * Returns only AVAILABLE future slots
     *
     * @param doctorId Doctor ID to get slots for
     * @return List of available slots
     */
    @GetMapping("/available/{doctorId}")
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
     * Get slots for the authenticated doctor
     *
     * @param userDetails Authenticated user (must be doctor)
     * @return List of doctor's slots
     */
    @GetMapping("/my-slots")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SlotResponse>> getMySlots(@AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting slots for doctor: {}", userDetails.getUsername());

        try {
            User doctorUser = userService.getUserByEmail(userDetails.getUsername());
            List<SlotResponse> slots = slotService.getDoctorSlots(doctorUser.getId());

            log.info("Found {} slots for doctor: {}", slots.size(), userDetails.getUsername());
            return ResponseEntity.ok(slots);

        } catch (Exception e) {
            log.error("Error getting doctor slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get slots by status for a doctor
     *
     * @param doctorId Doctor ID
     * @param status Slot status to filter by
     * @return List of slots with the specified status
     */
    @GetMapping("/doctor/{doctorId}/status/{status}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SlotResponse>> getSlotsByStatus(
            @PathVariable Long doctorId,
            @PathVariable String status) {

        log.info("Getting slots for doctor {} with status: {}", doctorId, status);

        try {
            SlotStatus slotStatus = SlotStatus.valueOf(status.toUpperCase());
            List<SlotResponse> slots = slotService.getSlotsByStatusAndDoctor(doctorId, slotStatus);

            log.info("Found {} slots with status {} for doctor {}", slots.size(), status, doctorId);
            return ResponseEntity.ok(slots);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting slots by status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get slots within a date range for a doctor
     *
     * @param doctorId Doctor ID
     * @param startDate Start date (ISO format)
     * @param endDate End date (ISO format)
     * @return List of slots in the date range
     */
    @GetMapping("/doctor/{doctorId}/date-range")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SlotResponse>> getSlotsByDateRange(
            @PathVariable Long doctorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Getting slots for doctor {} from {} to {}", doctorId, startDate, endDate);

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<SlotResponse> slots = slotService.getSlotsByDateRange(doctorId, start, end);
            log.info("Found {} slots in date range for doctor {}", slots.size(), doctorId);
            return ResponseEntity.ok(slots);

        } catch (Exception e) {
            log.error("Error getting slots by date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all slots in the system (Admin only)
     *
     * @return List of all slots
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SlotResponse>> getAllSlots() {

        log.info("Admin getting all slots");

        try {
            List<SlotResponse> slots = slotService.getAllSlots();
            log.info("Found {} total slots", slots.size());
            return ResponseEntity.ok(slots);

        } catch (Exception e) {
            log.error("Error getting all slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get slot by ID
     *
     * @param slotId Slot ID
     * @return Slot details
     */
    @GetMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('RH', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable Long slotId) {

        log.info("Getting slot: {}", slotId);

        try {
            SlotResponse slot = slotService.getSlotById(slotId);
            return ResponseEntity.ok(slot);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid slot ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clean up expired temporary locks (Admin use)
     *
     * @return Number of slots cleaned up
     */
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CleanupResponse> cleanupExpiredSlots() {

        log.info("Admin cleaning up expired temporary locks");

        try {
            int cleanedCount = slotService.cleanupExpiredTemporaryLocks();
            log.info("Cleaned up {} expired temporary locks", cleanedCount);

            return ResponseEntity.ok(new CleanupResponse(cleanedCount));

        } catch (Exception e) {
            log.error("Error cleaning up expired slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
