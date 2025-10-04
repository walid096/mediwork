package com.sqli.medwork.service.slot;

import com.sqli.medwork.dto.response.SlotResponse;
import com.sqli.medwork.dto.request.CreateSlotRequest;
import com.sqli.medwork.entity.Slot;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.enums.SlotStatus;
import com.sqli.medwork.repository.SlotRepository;
import com.sqli.medwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Service for managing medical appointment time slots
 *
 * Core functionality:
 * - Slot availability management for HR scheduling
 * - Slot status lifecycle (AVAILABLE → LOCKED → CONFIRMED)
 * - Doctor schedule management
 * - Expired slot cleanup and maintenance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    // ==================== CORE US1 FUNCTIONALITY ====================

    /**
     * Get available slots for HR to select when scheduling visits
     * Core US1 requirement: HR sees only AVAILABLE future slots
     */
    public List<SlotResponse> getAvailableSlotsForDoctor(Long doctorId) {
        log.info("Getting available slots for doctor: {}", doctorId);
        User doctor = validateAndGetDoctor(doctorId);

        List<Slot> availableSlots = slotRepository.findByDoctorIdAndStatusAndStartTimeAfter(
                doctorId, SlotStatus.AVAILABLE, LocalDateTime.now()
        );

        List<SlotResponse> responses = availableSlots.stream()
                .filter(slot -> !slot.isExpired())
                .map(this::buildSlotResponse)
                .collect(Collectors.toList());

        log.info("Found {} available slots for doctor: {}", responses.size(), doctorId);
        return responses;
    }

    /**
     * Lock slot when HR schedules a visit (US1 core functionality)
     * Changes status from AVAILABLE → TEMPORARILY_LOCKED
     */
    @Transactional
    public SlotResponse lockSlot(Long slotId) {
        log.info("Locking slot: {}", slotId);
        Slot slot = findSlotById(slotId);
        validateSlotCanBeLocked(slot);

        slot.setStatus(SlotStatus.TEMPORARILY_LOCKED);
        slot.setUpdatedAt(LocalDateTime.now());
        Slot savedSlot = slotRepository.save(slot);

        log.info("Slot locked successfully: ID={}, Status={}", slotId, savedSlot.getStatus());
        return buildSlotResponse(savedSlot);
    }

    /**
     * Confirm slot when doctor accepts visit (US1 workflow)
     * Changes status from TEMPORARILY_LOCKED → CONFIRMED
     */
    @Transactional
    public SlotResponse confirmSlot(Long slotId) {
        log.info("Confirming slot: {}", slotId);
        Slot slot = findSlotById(slotId);
        validateSlotCanBeConfirmed(slot);

        slot.setStatus(SlotStatus.CONFIRMED);
        slot.setUpdatedAt(LocalDateTime.now());
        Slot savedSlot = slotRepository.save(slot);

        log.info("Slot confirmed successfully: ID={}, Status={}", slotId, savedSlot.getStatus());
        return buildSlotResponse(savedSlot);
    }

    /**
     * Release slot when visit is cancelled (US1 workflow)
     * Changes status back to AVAILABLE
     */
    @Transactional
    public SlotResponse releaseSlot(Long slotId) {
        log.info("Releasing slot: {}", slotId);
        Slot slot = findSlotById(slotId);
        validateSlotCanBeReleased(slot);

        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());
        Slot savedSlot = slotRepository.save(slot);

        log.info("Slot released successfully: ID={}, Status={}", slotId, savedSlot.getStatus());
        return buildSlotResponse(savedSlot);
    }

    // ==================== SLOT CREATION AND MANAGEMENT ====================

    /**
     * Create a new time slot (Doctor use)
     *
     * Business Rules:
     * - Only doctors can create slots for themselves
     * - Slots must be in the future
     * - No overlapping slots allowed
     * - Initial status is AVAILABLE
     */
    @Transactional
    public SlotResponse createSlot(CreateSlotRequest request, User doctorUser) {
        log.info("Doctor {} creating slot: {} to {}",
                doctorUser.getEmail(), request.getStartTime(), request.getEndTime());

        // Validate doctor role
        if (!Role.DOCTOR.equals(doctorUser.getRole()) && !Role.ADMIN.equals(doctorUser.getRole())) {
            throw new IllegalStateException("Only doctors can create slots");
        }

        // Validate time parameters
        validateTimeParameters(request.getStartTime(), request.getEndTime());

        // Check for overlapping slots
        validateNoOverlappingSlots(doctorUser.getId(), request.getStartTime(), request.getEndTime());

        // Create the slot
        Slot slot = Slot.builder()
                .doctor(doctorUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(SlotStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();

        Slot savedSlot = slotRepository.save(slot);
        log.info("Slot created successfully: ID={}", savedSlot.getId());

        return buildSlotResponse(savedSlot);
    }



    /**
     * Update slot status (Doctor use)
     */
    @Transactional
    public SlotResponse updateSlotStatus(Long slotId, SlotStatus status, User doctorUser) {
        log.info("Doctor {} updating slot {} status to: {}",
                doctorUser.getEmail(), slotId, status);

        Slot slot = findSlotById(slotId);
        validateSlotOwnership(slot, doctorUser);
        validateStatusTransition(slot.getStatus(), status);

        slot.setStatus(status);
        slot.setUpdatedAt(LocalDateTime.now());
        Slot savedSlot = slotRepository.save(slot);

        log.info("Slot status updated successfully: ID={}, Status={}", slotId, savedSlot.getStatus());
        return buildSlotResponse(savedSlot);
    }

    /**
     * Delete a slot (Doctor use)
     */
    @Transactional
    public void deleteSlot(Long slotId, User doctorUser) {
        log.info("Doctor {} deleting slot: {}", doctorUser.getEmail(), slotId);

        Slot slot = findSlotById(slotId);
        validateSlotOwnership(slot, doctorUser);
        validateSlotCanBeDeleted(slot);

        slotRepository.delete(slot);
        log.info("Slot deleted successfully: ID={}", slotId);
    }

    /**
     * Create available slots for a doctor (Admin functionality)
     * Used to set up doctor's schedule
     */
    @Transactional
    public List<SlotResponse> createAvailableSlots(Long doctorId, LocalDateTime startTime,
                                                   LocalDateTime endTime, int slotDurationMinutes) {
        log.info("Creating slots for doctor: {} from {} to {} ({} min intervals)",
                doctorId, startTime, endTime, slotDurationMinutes);

        User doctor = validateAndGetDoctor(doctorId);
        validateTimeParameters(startTime, endTime, slotDurationMinutes);
        validateNoOverlappingSlots(doctorId, startTime, endTime);

        List<Slot> createdSlots = createSlotsInTimeRange(doctor, startTime, endTime, slotDurationMinutes);
        List<Slot> savedSlots = slotRepository.saveAll(createdSlots);

        log.info("Created {} slots for doctor: {}", savedSlots.size(), doctorId);
        return savedSlots.stream().map(this::buildSlotResponse).collect(Collectors.toList());
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get all slots for a doctor
     */
    public List<SlotResponse> getDoctorSlots(Long doctorId) {
        return slotRepository.findByDoctorId(doctorId).stream()
                .map(this::buildSlotResponse).collect(Collectors.toList());
    }

    /**
     * Get slots by status for a doctor
     */
    public List<SlotResponse> getSlotsByStatusAndDoctor(Long doctorId, SlotStatus status) {
        return slotRepository.findByDoctorIdAndStatus(doctorId, status).stream()
                .map(this::buildSlotResponse).collect(Collectors.toList());
    }

    /**
     * Get slots within a date range for a doctor
     */
    public List<SlotResponse> getSlotsByDateRange(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return slotRepository.findByDoctorIdAndDateRange(doctorId, startDate, endDate).stream()
                .map(this::buildSlotResponse).collect(Collectors.toList());
    }

    /**
     * Get slot by ID
     */
    public SlotResponse getSlotById(Long slotId) {
        Slot slot = findSlotById(slotId);
        return buildSlotResponse(slot);
    }

    /**
     * Get all slots in the system (Admin use)
     */
    public List<SlotResponse> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(this::buildSlotResponse).collect(Collectors.toList());
    }

    // ==================== MAINTENANCE ====================

    /**
     * Clean up expired TEMPORARILY_LOCKED slots (US1 requirement: 2-hour expiration)
     * This should be called by a scheduled task
     */
    @Transactional
    public int cleanupExpiredLocks() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<Slot> expiredSlots = slotRepository.findByStatusAndStartTimeBefore(SlotStatus.TEMPORARILY_LOCKED, cutoffTime);

        if (!expiredSlots.isEmpty()) {
            expiredSlots.forEach(slot -> {
                slot.setStatus(SlotStatus.AVAILABLE);
                slot.setUpdatedAt(LocalDateTime.now());
            });
            slotRepository.saveAll(expiredSlots);
            log.info("Cleaned up {} expired locked slots", expiredSlots.size());
        }

        return expiredSlots.size();
    }

    /**
     * Clean up expired temporary locks (Admin use)
     */
    @Transactional
    public int cleanupExpiredTemporaryLocks() {
        return cleanupExpiredLocks();
    }

    // ==================== VALIDATIONS ====================

    private User validateAndGetDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        if (!Role.DOCTOR.equals(doctor.getRole())) {
            throw new IllegalArgumentException("User is not a doctor: " + doctorId);
        }
        if (doctor.isArchived()) {
            throw new IllegalStateException("Doctor is archived: " + doctorId);
        }
        return doctor;
    }

    private void validateTimeParameters(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    private void validateTimeParameters(LocalDateTime startTime, LocalDateTime endTime, int slotDurationMinutes) {
        validateTimeParameters(startTime, endTime);
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("Slot duration must be positive");
        }
    }

    private void validateNoOverlappingSlots(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasOverlap = slotRepository.existsByDoctorIdAndStartTimeBetweenAndStatusIn(
                doctorId, startTime, endTime,
                List.of(SlotStatus.AVAILABLE, SlotStatus.TEMPORARILY_LOCKED, SlotStatus.CONFIRMED)
        );
        if (hasOverlap) {
            throw new IllegalStateException("Overlapping slots exist for doctor: " + doctorId);
        }
    }

    private void validateSlotCanBeLocked(Slot slot) {
        if (!SlotStatus.AVAILABLE.equals(slot.getStatus())) {
            throw new IllegalStateException("Cannot lock slot: status is " + slot.getStatus());
        }
        if (slot.isExpired()) {
            throw new IllegalStateException("Cannot lock expired slot");
        }
    }

    private void validateSlotCanBeConfirmed(Slot slot) {
        if (!SlotStatus.TEMPORARILY_LOCKED.equals(slot.getStatus())) {
            throw new IllegalArgumentException("Cannot confirm slot: status is " + slot.getStatus());
        }
        if (slot.isExpired()) {
            throw new IllegalStateException("Cannot confirm expired slot");
        }
    }

    private void validateSlotCanBeReleased(Slot slot) {
        if (!SlotStatus.TEMPORARILY_LOCKED.equals(slot.getStatus()) &&
                !SlotStatus.CONFIRMED.equals(slot.getStatus())) {
            throw new IllegalStateException("Cannot release slot: status is " + slot.getStatus());
        }
    }

    private void validateSlotOwnership(Slot slot, User doctorUser) {
        if (!slot.getDoctor().getId().equals(doctorUser.getId()) &&
                !Role.ADMIN.equals(doctorUser.getRole())) {
            throw new IllegalStateException("Doctor can only manage their own slots");
        }
    }

    private void validateStatusTransition(SlotStatus currentStatus, SlotStatus newStatus) {
        // Define valid status transitions
        if (currentStatus == SlotStatus.AVAILABLE && newStatus == SlotStatus.UNAVAILABLE) {
            return; // Valid: Mark as unavailable
        }
        if (currentStatus == SlotStatus.UNAVAILABLE && newStatus == SlotStatus.AVAILABLE) {
            return; // Valid: Mark as available again
        }
        if (currentStatus == SlotStatus.AVAILABLE && newStatus == SlotStatus.AVAILABLE) {
            return; // Valid: No change
        }

        throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
    }

    private void validateSlotCanBeDeleted(Slot slot) {
        if (SlotStatus.CONFIRMED.equals(slot.getStatus())) {
            throw new IllegalStateException("Cannot delete confirmed slot");
        }
        if (SlotStatus.TEMPORARILY_LOCKED.equals(slot.getStatus())) {
            throw new IllegalStateException("Cannot delete temporarily locked slot");
        }
    }

    private Slot findSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
    }

    // ==================== HELPER METHODS ====================

    private List<Slot> createSlotsInTimeRange(User doctor, LocalDateTime startTime, LocalDateTime endTime, int slotDurationMinutes) {
        List<Slot> slots = new ArrayList<>();
        LocalDateTime currentTime = startTime;

        while (!currentTime.plusMinutes(slotDurationMinutes).isAfter(endTime)) {
            slots.add(Slot.builder()
                    .doctor(doctor)
                    .startTime(currentTime)
                    .endTime(currentTime.plusMinutes(slotDurationMinutes))
                    .status(SlotStatus.AVAILABLE)
                    .createdAt(LocalDateTime.now())
                    .build());
            currentTime = currentTime.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    private SlotResponse buildSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .doctor(SlotResponse.DoctorInfoDto.builder()
                        .id(slot.getDoctor().getId())
                        .firstName(slot.getDoctor().getFirstName())
                        .lastName(slot.getDoctor().getLastName())
                        .email(slot.getDoctor().getEmail())
                        .matricule(slot.getDoctor().getMatricule())
                        .build())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}