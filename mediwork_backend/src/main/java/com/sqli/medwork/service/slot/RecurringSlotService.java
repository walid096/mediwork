package com.sqli.medwork.service.slot;

import com.sqli.medwork.dto.request.CreateRecurringSlotRequest;
import com.sqli.medwork.dto.response.RecurringSlotResponse;
import com.sqli.medwork.dto.request.UpdateRecurringSlotRequest;
import com.sqli.medwork.dto.response.VisitResponse;
import com.sqli.medwork.entity.RecurringSlot;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.repository.RecurringSlotRepository;
import com.sqli.medwork.service.common.LogService;
import com.sqli.medwork.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringSlotService {

    private final RecurringSlotRepository recurringSlotRepository;
    private final UserService userService;
    private final LogService logService;

    // ==================== CREATE RECURRING SLOT ====================

    /**
     * Create a new recurring slot for a doctor
     *
     * Business Rules:
     * - Only doctors can create recurring slots for themselves
     * - No overlapping time ranges on the same day
     * - Start time must be before end time
     * - Doctor must exist and be active
     *
     * @param request Recurring slot creation request
     * @param doctorEmail Email of the authenticated doctor
     * @return Created recurring slot response
     */
    @Transactional
    public RecurringSlotResponse createRecurringSlot(CreateRecurringSlotRequest request, String doctorEmail) {
        log.info("Creating recurring slot for doctor {}: {} {} to {}",
                doctorEmail, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        // Validate request
        validateCreateRequest(request);

        // Get doctor user
        User doctor = userService.getUserByEmail(doctorEmail);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found: " + doctorEmail);
        }

        // Check for overlapping slots
        List<RecurringSlot> overlappingSlots = recurringSlotRepository.findOverlappingSlots(
                doctor, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        if (!overlappingSlots.isEmpty()) {
            throw new RuntimeException("Overlapping recurring slot already exists for " +
                    request.getDayOfWeek() + " between " + request.getStartTime() + " and " + request.getEndTime());
        }

        // Create and save recurring slot
        RecurringSlot recurringSlot = RecurringSlot.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        RecurringSlot savedSlot = recurringSlotRepository.save(recurringSlot);

        // Log the action
        logService.log(LogActionType.RECURRING_SLOT_CREATED,
                "Recurring slot created for doctor: " + doctorEmail + " on " + request.getDayOfWeek());

        log.info("Recurring slot created successfully: ID={}", savedSlot.getId());
        return buildRecurringSlotResponse(savedSlot);
    }

    // ==================== GET RECURRING SLOTS ====================

    /**
     * Get all recurring slots for a doctor
     *
     * @param doctorEmail Email of the authenticated doctor
     * @return List of recurring slot responses
     */
    @Transactional(readOnly = true)
    public List<RecurringSlotResponse> getRecurringSlotsForDoctor(String doctorEmail) {
        log.info("Fetching recurring slots for doctor: {}", doctorEmail);

        User doctor = userService.getUserByEmail(doctorEmail);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found: " + doctorEmail);
        }

        List<RecurringSlot> slots = recurringSlotRepository.findByDoctorOrderByDayOfWeekAscStartTimeAsc(doctor);

        log.info("Found {} recurring slots for doctor: {}", slots.size(), doctorEmail);
        return slots.stream()
                .map(this::buildRecurringSlotResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all recurring slots for a specific doctor by ID (RH/Admin use)
     *
     * @param doctorId ID of the doctor
     * @return List of recurring slot responses
     */
    @Transactional(readOnly = true)
    public List<RecurringSlotResponse> getRecurringSlotsForDoctorById(Long doctorId) {
        log.info("Fetching recurring slots for doctor ID: {}", doctorId);

        User doctor = userService.getUserById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found with ID: " + doctorId);
        }

        if (!Role.DOCTOR.equals(doctor.getRole())) {
            throw new RuntimeException("User is not a doctor: " + doctorId);
        }

        List<RecurringSlot> slots = recurringSlotRepository.findByDoctorOrderByDayOfWeekAscStartTimeAsc(doctor);

        log.info("Found {} recurring slots for doctor ID: {}", slots.size(), doctorId);
        return slots.stream()
                .map(this::buildRecurringSlotResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE RECURRING SLOT ====================

    /**
     * Update an existing recurring slot
     *
     * @param slotId ID of the slot to update
     * @param request Update request
     * @param doctorEmail Email of the authenticated doctor
     * @return Updated recurring slot response
     */
    @Transactional
    public RecurringSlotResponse updateRecurringSlot(Long slotId, UpdateRecurringSlotRequest request, String doctorEmail) {
        log.info("Updating recurring slot {} for doctor {}: {} {} to {}",
                slotId, doctorEmail, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        // Validate request
        validateUpdateRequest(request);

        // Get doctor user
        User doctor = userService.getUserByEmail(doctorEmail);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found: " + doctorEmail);
        }

        // Find and verify ownership
        RecurringSlot existingSlot = recurringSlotRepository.findByIdAndDoctor(slotId, doctor)
                .orElseThrow(() -> new RuntimeException("Recurring slot not found or access denied"));

        // Check for overlapping slots (excluding current slot)
        List<RecurringSlot> overlappingSlots = recurringSlotRepository.findOverlappingSlots(
                doctor, request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        overlappingSlots = overlappingSlots.stream()
                .filter(slot -> !slot.getId().equals(slotId))
                .collect(Collectors.toList());

        if (!overlappingSlots.isEmpty()) {
            throw new RuntimeException("Overlapping recurring slot already exists for " +
                    request.getDayOfWeek() + " between " + request.getStartTime() + " and " + request.getEndTime());
        }

        // ✅ FIXED: Now correctly sets all three fields
        existingSlot.setDayOfWeek(request.getDayOfWeek());
        existingSlot.setStartTime(request.getStartTime());  // ✅ FIXED: Was wrong before
        existingSlot.setEndTime(request.getEndTime());

        RecurringSlot updatedSlot = recurringSlotRepository.save(existingSlot);

        // Log the action
        logService.log(LogActionType.RECURRING_SLOT_UPDATED,
                "Recurring slot updated for doctor: " + doctorEmail + " on " + request.getDayOfWeek());

        log.info("Recurring slot updated successfully: ID={}", updatedSlot.getId());
        return buildRecurringSlotResponse(updatedSlot);
    }

    // ==================== DELETE RECURRING SLOT ====================

    /**
     * Delete a recurring slot
     *
     * @param slotId ID of the slot to delete
     * @param doctorEmail Email of the authenticated doctor
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteRecurringSlot(Long slotId, String doctorEmail) {
        log.info("Deleting recurring slot {} for doctor: {}", slotId, doctorEmail);

        User doctor = userService.getUserByEmail(doctorEmail);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found: " + doctorEmail);
        }

        RecurringSlot slot = recurringSlotRepository.findByIdAndDoctor(slotId, doctor)
                .orElse(null);

        if (slot == null) {
            log.warn("Recurring slot {} not found or access denied for doctor: {}", slotId, doctorEmail);
            return false;
        }

        recurringSlotRepository.delete(slot);

        // Log the action
        logService.log(LogActionType.RECURRING_SLOT_DELETED,
                "Recurring slot deleted for doctor: " + doctorEmail + " on " + slot.getDayOfWeek());

        log.info("Recurring slot deleted successfully: ID={}", slotId);
        return true;
    }

    // ==================== VALIDATION METHODS ====================

    private void validateCreateRequest(CreateRecurringSlotRequest request) {
        if (request.getDayOfWeek() == null) {
            throw new RuntimeException("Day of week is required");
        }
        if (request.getStartTime() == null) {
            throw new RuntimeException("Start time is required");
        }
        if (request.getEndTime() == null) {
            throw new RuntimeException("End time is required");
        }
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }
    }

    private void validateUpdateRequest(UpdateRecurringSlotRequest request) {
        validateCreateRequest(new CreateRecurringSlotRequest(
                request.getDayOfWeek(), request.getStartTime(), request.getEndTime()));
    }

    // ==================== HELPER METHODS ====================

    private RecurringSlotResponse buildRecurringSlotResponse(RecurringSlot slot) {
        return RecurringSlotResponse.builder()
                .id(slot.getId())
                .doctor(buildUserInfoDto(slot.getDoctor()))
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }

    private VisitResponse.UserInfoDto buildUserInfoDto(User user) {
        return VisitResponse.UserInfoDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .matricule(user.getMatricule())
                .build();
    }
}