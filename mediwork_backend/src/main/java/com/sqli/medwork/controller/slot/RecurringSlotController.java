package com.sqli.medwork.controller.slot;

import com.sqli.medwork.dto.request.CreateRecurringSlotRequest;
import com.sqli.medwork.dto.response.RecurringSlotResponse;
import com.sqli.medwork.dto.request.UpdateRecurringSlotRequest;
import com.sqli.medwork.service.slot.RecurringSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller for managing recurring medical appointment slots
 *
 * Core functionality:
 * - Doctor recurring slot management (create, read, update, delete)
 * - Automatic slot generation based on recurring patterns
 * - Conflict prevention and validation
 *
 * Security:
 * - Doctors can manage their own recurring slots
 * - Admin has full access to all recurring slots
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recurring-slots")
public class RecurringSlotController {

    private final RecurringSlotService recurringSlotService;

    // ==================== CREATE RECURRING SLOT ====================

    /**
     * Create a new recurring slot (Doctor use)
     *
     * Business Rules:
     * - Only doctors can create recurring slots for themselves
     * - No overlapping time ranges on the same day
     * - Start time must be before end time
     *
     * @param request Recurring slot creation request
     * @param userDetails Authenticated user (must be doctor)
     * @return Created recurring slot response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<RecurringSlotResponse> createRecurringSlot(
            @Valid @RequestBody CreateRecurringSlotRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} creating recurring slot: {} {} to {}",
                userDetails.getUsername(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        try {
            RecurringSlotResponse slot = recurringSlotService.createRecurringSlot(request, userDetails.getUsername());

            log.info("Recurring slot created successfully: ID={}", slot.getId());
            return ResponseEntity.ok(slot);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid recurring slot request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating recurring slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== GET RECURRING SLOTS ====================

    /**
     * Get all recurring slots for the authenticated doctor
     *
     * @param userDetails Authenticated user (must be doctor)
     * @return List of recurring slot responses
     */
    @GetMapping("/my-recurring-slots")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<RecurringSlotResponse>> getMyRecurringSlots(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} fetching their recurring slots", userDetails.getUsername());

        try {
            List<RecurringSlotResponse> slots = recurringSlotService.getRecurringSlotsForDoctor(userDetails.getUsername());

            log.info("Found {} recurring slots for doctor: {}", slots.size(), userDetails.getUsername());
            return ResponseEntity.ok(slots);

        } catch (Exception e) {
            log.error("Error fetching recurring slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all recurring slots for a specific doctor by ID (RH/Admin use)
     *
     * @param doctorId ID of the doctor
     * @return List of recurring slot responses
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<RecurringSlotResponse>> getRecurringSlotsForDoctor(
            @PathVariable Long doctorId) {

        log.info("RH/Admin requesting recurring slots for doctor ID: {}", doctorId);

        try {
            List<RecurringSlotResponse> slots = recurringSlotService.getRecurringSlotsForDoctorById(doctorId);
            log.info("Found {} recurring slots for doctor ID: {}", slots.size(), doctorId);
            return ResponseEntity.ok(slots);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid doctor ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching recurring slots for doctor ID {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== UPDATE RECURRING SLOT ====================

    /**
     * Update an existing recurring slot (Doctor use)
     *
     * @param slotId ID of the slot to update
     * @param request Update request
     * @param userDetails Authenticated user (must be doctor)
     * @return Updated recurring slot response
     */
    @PutMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<RecurringSlotResponse> updateRecurringSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody UpdateRecurringSlotRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} updating recurring slot {}: {} {} to {}",
                userDetails.getUsername(), slotId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        try {
            RecurringSlotResponse slot = recurringSlotService.updateRecurringSlot(slotId, request, userDetails.getUsername());

            log.info("Recurring slot updated successfully: ID={}", slotId);
            return ResponseEntity.ok(slot);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Business rule violation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating recurring slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DELETE RECURRING SLOT ====================

    /**
     * Delete a recurring slot (Doctor use)
     *
     * @param slotId ID of the slot to delete
     * @param userDetails Authenticated user (must be doctor)
     * @return Success response
     */
    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteRecurringSlot(
            @PathVariable Long slotId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Doctor {} deleting recurring slot: {}", userDetails.getUsername(), slotId);

        try {
            boolean deleted = recurringSlotService.deleteRecurringSlot(slotId, userDetails.getUsername());

            if (deleted) {
                log.info("Recurring slot deleted successfully: ID={}", slotId);
                return ResponseEntity.ok().build();
            } else {
                log.warn("Recurring slot not found or access denied: ID={}", slotId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting recurring slot: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all recurring slots for all doctors (Admin use)
     *
     * @return List of all recurring slot responses
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RecurringSlotResponse>> getAllRecurringSlots() {

        log.info("Admin fetching all recurring slots");

        try {
            // TODO: Implement admin service method to get all slots
            // For now, return empty list
            return ResponseEntity.ok(List.of());

        } catch (Exception e) {
            log.error("Error fetching all recurring slots: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}