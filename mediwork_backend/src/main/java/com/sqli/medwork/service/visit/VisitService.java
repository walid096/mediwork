package com.sqli.medwork.service.visit;

import com.sqli.medwork.dto.request.VisitRequest;
import com.sqli.medwork.dto.response.VisitResponse;
import com.sqli.medwork.dto.request.CreateVisitWithSlotRequest;
import com.sqli.medwork.dto.response.VisitWithSlotResponse;
import com.sqli.medwork.entity.Slot;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.entity.Visit;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.enums.SlotStatus;
import com.sqli.medwork.enums.VisitStatus;

import java.time.format.DateTimeParseException;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.repository.SlotRepository;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.repository.VisitRepository;
import com.sqli.medwork.service.common.LogService;
import com.sqli.medwork.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing medical visits
 *
 * Core functionality:
 * - HR visit scheduling with slot locking
 * - Doctor visit confirmation
 * - Visit status management
 * - Conflict prevention and validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final LogService logService;
    private final UserService userService;

    // ==================== CORE US1 FUNCTIONALITY ====================

    /**
     * Create a new medical visit (HR scheduling)
     *
     * Business Rules:
     * - HR role validation
     * - Slot availability validation
     * - Conflict prevention (double-booking)
     * - Automatic slot locking
     *
     * @param request Visit creation request
     * @param hrUser HR user creating the visit
     * @return Created visit response
     */
    @Transactional
    public VisitResponse createVisit(VisitRequest request, User hrUser) {
        log.info("Creating visit: {}", request.getRequestSummary());

        // 1. Validate HR role
        validateHRRole(hrUser);

        // 2. Validate and get entities
        User collaborator = validateAndGetUser(request.getCollaboratorId(), Role.COLLABORATOR);
        User doctor = validateAndGetUser(request.getDoctorId(), Role.DOCTOR);
        Slot slot = validateAndGetAvailableSlot(request.getSlotId());

        // 3. Check for conflicts
        validateNoConflicts(collaborator, doctor, slot);

        // 4. Lock the slot
        lockSlot(slot);

        // 5. Create the visit
        Visit visit = createVisitEntity(request, collaborator, doctor, slot, hrUser);
        Visit savedVisit = visitRepository.save(visit);

        // 6. Log the action
        logService.log(LogActionType.SCHEDULE_VISITE,
                "Visit created: ID=" + savedVisit.getId() + ", Collaborator=" + collaborator.getEmail() + ", Doctor=" + doctor.getEmail());

        log.info("Visit created successfully: ID={}, Collaborator={}, Doctor={}, Slot={}",
                savedVisit.getId(), collaborator.getEmail(), doctor.getEmail(), slot.getId());

        return buildVisitResponse(savedVisit);
    }

    // ✅ ADDED: Create visit with new slot (HR scheduling)
    /**
     * Create a new medical visit with a new slot
     *
     * Business Rules:
     * - HR role validation
     * - Creates both slot and visit in one transaction
     * - Slot conflict validation
     * - Time validation (future, reasonable duration)
     * - Automatic slot creation and visit scheduling
     *
     * @param request Visit with slot creation request
     * @param hrUser HR user creating the visit
     * @return Created visit with slot response
     */
    @Transactional
    public VisitWithSlotResponse createVisitWithSlot(CreateVisitWithSlotRequest request, User hrUser) {
        log.info("Creating visit with slot: {}", request.getRequestSummary());

        // 1. Validate HR role
        validateHRRole(hrUser);

        // 2. Validate and get entities
        User collaborator = validateAndGetUser(request.getCollaboratorId(), Role.COLLABORATOR);
        User doctor = validateAndGetUser(request.getDoctorId(), Role.DOCTOR);

        // 3. Validate time constraints
        validateTimeConstraints(request);

        // 4. Check for slot conflicts
        validateNoSlotConflicts(doctor, request.getStartTime(), request.getEndTime());

        // 5. Create the slot
        Slot slot = createSlotEntity(request, doctor);
        Slot savedSlot = slotRepository.save(slot);

        // 6. Create the visit
        Visit visit = createVisitEntity(request, collaborator, doctor, savedSlot, hrUser);
        Visit savedVisit = visitRepository.save(visit);

        // 7. Log the action
        logService.log(LogActionType.SCHEDULE_VISITE,
                "Visit with slot created: Visit ID=" + savedVisit.getId() + ", Slot ID=" + savedSlot.getId() +
                        ", Collaborator=" + collaborator.getEmail() + ", Doctor=" + doctor.getEmail());

        log.info("Visit with slot created successfully: Visit ID={}, Slot ID={}, Collaborator={}, Doctor={}, Slot={}",
                savedVisit.getId(), savedSlot.getId(), collaborator.getEmail(), doctor.getEmail(), savedSlot.getId());

        return buildVisitWithSlotResponse(savedVisit, savedSlot);
    }

    /**
     * Confirm a visit (Doctor acceptance)
     *
     * @param visitId Visit ID to confirm
     * @param doctorUser Doctor confirming the visit
     * @return Updated visit response
     */
    @Transactional
    public VisitResponse confirmVisit(Long visitId, User doctorUser) {
        log.info("Doctor {} confirming visit: {}", doctorUser.getEmail(), visitId);

        Visit visit = getVisitById(visitId);
        validateDoctorOwnership(visit, doctorUser);
        validateVisitCanBeConfirmed(visit);

        // Update slot status to CONFIRMED
        Slot slot = visit.getSlot();
        slot.setStatus(SlotStatus.CONFIRMED);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);

        // Update visit status
        visit.setStatus(VisitStatus.SCHEDULED);
        visit.setUpdatedAt(LocalDateTime.now());
        Visit savedVisit = visitRepository.save(visit);

        // Log the action
        logService.log(LogActionType.VALIDATE_VISITE, "Visit confirmed: ID=" + visitId + ", Doctor=" + doctorUser.getEmail());

        log.info("Visit confirmed: ID={}, Doctor={}", visitId, doctorUser.getEmail());
        return buildVisitResponse(savedVisit);
    }

    /**
     * Cancel a visit
     *
     * @param visitId Visit ID to cancel
     * @param user User requesting cancellation
     * @return Updated visit response
     */
    @Transactional
    public VisitResponse cancelVisit(Long visitId, User user) {
        log.info("User {} cancelling visit: {}", user.getEmail(), visitId);

        Visit visit = getVisitById(visitId);
        validateCancellationPermission(visit, user);

        // Release the slot
        Slot slot = visit.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);

        // Update visit status
        visit.setStatus(VisitStatus.CANCELLED);
        visit.setUpdatedAt(LocalDateTime.now());
        Visit savedVisit = visitRepository.save(visit);

        // Log the action
        logService.log(LogActionType.CANCEL_VISITE, "Visit cancelled: ID=" + visitId + ", User=" + user.getEmail());

        log.info("Visit cancelled: ID={}, User={}", visitId, user.getEmail());
        return buildVisitResponse(savedVisit);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get visits for a doctor
     */
    public List<VisitResponse> getDoctorVisits(Long doctorId) {
        List<Visit> visits = visitRepository.findByDoctorId(doctorId);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get visits for a collaborator
     */
    public List<VisitResponse> getCollaboratorVisits(Long collaboratorId) {
        List<Visit> visits = visitRepository.findByCollaboratorId(collaboratorId);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get visits by status
     */
    public List<VisitResponse> getVisitsByStatus(VisitStatus status) {
        List<Visit> visits = visitRepository.findByStatus(status);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get visits created by a specific HR user
     */
    public List<VisitResponse> getVisitsByCreatedBy(Long createdById) {
        List<Visit> visits = visitRepository.findByCreatedById(createdById);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all visits in the system (Admin use)
     */
    public List<VisitResponse> getAllVisits() {
        List<Visit> visits = visitRepository.findAll();
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get visit by ID with access control validation
     */
    public VisitResponse getVisitById(Long visitId, User user) {
        Visit visit = getVisitById(visitId);

        // Check access permissions
        if (!visit.getCreatedBy().getId().equals(user.getId()) &&
                !visit.getDoctor().getId().equals(user.getId()) &&
                !visit.getCollaborator().getId().equals(user.getId()) &&
                !Role.ADMIN.equals(user.getRole())) {
            throw new IllegalStateException("Access denied to visit");
        }

        return buildVisitResponse(visit);
    }

    /**
     * Get visits within a date range (Admin/RH reporting)
     */
    public List<VisitResponse> getVisitsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Visit> visits = visitRepository.findBySlotStartTimeBetween(start, end);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    // ✅ FIXED: Get visits by doctor and date for conflict checking
    /**
     * Get all visits for a specific doctor on a specific date
     *
     * Used by RH to check for conflicts when selecting time slots
     * Returns visits that could conflict with new scheduling
     *
     * @param doctorId Doctor ID
     * @param date Date string in YYYY-MM-DD format
     * @return List of visits on that date
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByDoctorAndDate(Long doctorId, String date) {
        log.info("Fetching visits for doctor {} on date: {}", doctorId, date);

        // ✅ FIXED: Date validation with try-catch
        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format received: {}", date);
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD: " + date);
        }

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

        // Get doctor user
        User doctor = userService.getUserById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found with ID: " + doctorId);
        }

        // ✅ FIXED: Role validation
        if (!Role.DOCTOR.equals(doctor.getRole())) {
            throw new IllegalArgumentException("User with ID " + doctorId + " is not a doctor");
        }

        // ✅ FIXED: Find visits in the date range with statuses parameter
        List<Visit> visits = visitRepository.findByDoctorAndStartTimeBetween(
                doctor,
                startOfDay,
                endOfDay,
                List.of(VisitStatus.PENDING_DOCTOR_CONFIRMATION, VisitStatus.SCHEDULED, VisitStatus.IN_PROGRESS)
        );

        log.info("Found {} visits for doctor {} on date {}", visits.size(), doctorId, date);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate HR role for visit creation
     */
    private void validateHRRole(User user) {
        if (!Role.RH.equals(user.getRole()) && !Role.ADMIN.equals(user.getRole())) {
            throw new IllegalStateException("Only HR users can create visits");
        }
    }

    /**
     * Validate and get user by ID and role
     */
    private User validateAndGetUser(Long userId, Role expectedRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!expectedRole.equals(user.getRole())) {
            throw new IllegalArgumentException("User " + user.getEmail() + " is not a " + expectedRole);
        }

        if (user.isArchived()) {
            throw new IllegalStateException("User " + user.getEmail() + " is archived");
        }

        return user;
    }

    /**
     * Validate and get available slot
     */
    private Slot validateAndGetAvailableSlot(Long slotId) {
        Slot slot = slotRepository.findByIdAndStatus(slotId, SlotStatus.AVAILABLE)
                .orElseThrow(() -> new IllegalArgumentException("Slot not available: " + slotId));

        if (slot.isExpired()) {
            throw new IllegalStateException("Slot has expired: " + slotId);
        }

        return slot;
    }

    /**
     * Validate no conflicts exist
     */
    private void validateNoConflicts(User collaborator, User doctor, Slot slot) {
        // Check collaborator conflicts
        if (visitRepository.hasConflictingVisits(
                collaborator.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                List.of(VisitStatus.PENDING_DOCTOR_CONFIRMATION, VisitStatus.SCHEDULED, VisitStatus.IN_PROGRESS)
        )) {
            throw new IllegalStateException("Collaborator has conflicting visits");
        }

        // Check doctor conflicts
        if (visitRepository.doctorHasConflictingVisits(
                doctor.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                List.of(VisitStatus.PENDING_DOCTOR_CONFIRMATION, VisitStatus.SCHEDULED, VisitStatus.IN_PROGRESS)
        )) {
            throw new IllegalStateException("Doctor has conflicting visits");
        }
    }

    /**
     * Lock slot for visit
     */
    private void lockSlot(Slot slot) {
        slot.setStatus(SlotStatus.TEMPORARILY_LOCKED);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);
        log.info("Slot locked: ID={}, Status={}", slot.getId(), slot.getStatus());
    }

    /**
     * Create visit entity
     */
    private Visit createVisitEntity(VisitRequest request, User collaborator, User doctor, Slot slot, User hrUser) {
        return Visit.builder()
                .collaborator(collaborator)
                .doctor(doctor)
                .slot(slot)
                .visitType(request.getVisitType())
                .status(VisitStatus.PENDING_DOCTOR_CONFIRMATION)
                .createdBy(hrUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get visit by ID with validation
     */
    private Visit getVisitById(Long visitId) {
        return visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));
    }

    /**
     * Validate doctor ownership of visit
     */
    private void validateDoctorOwnership(Visit visit, User doctorUser) {
        if (!visit.getDoctor().getId().equals(doctorUser.getId())) {
            throw new IllegalStateException("Doctor can only confirm their own visits");
        }
    }

    /**
     * Validate visit can be confirmed
     */
    private void validateVisitCanBeConfirmed(Visit visit) {
        if (!VisitStatus.PENDING_DOCTOR_CONFIRMATION.equals(visit.getStatus())) {
            throw new IllegalStateException("Only pending visits can be confirmed");
        }
    }

    /**
     * Validate cancellation permission
     */
    private void validateCancellationPermission(Visit visit, User user) {
        boolean canCancel = visit.canBeCancelled();
        boolean isOwner = visit.getCreatedBy().getId().equals(user.getId()) ||
                visit.getDoctor().getId().equals(user.getId()) ||
                Role.ADMIN.equals(user.getRole());

        if (!canCancel || !isOwner) {
            throw new IllegalStateException("Visit cannot be cancelled or user lacks permission");
        }
    }

    /**
     * Build visit response DTO
     */
    private VisitResponse buildVisitResponse(Visit visit) {
        return VisitResponse.builder()
                .id(visit.getId())
                .collaborator(buildUserInfoDto(visit.getCollaborator()))
                .doctor(buildUserInfoDto(visit.getDoctor()))
                .slot(buildSlotInfoDto(visit.getSlot()))
                .visitType(visit.getVisitType())
                .status(visit.getStatus())
                .createdBy(buildUserInfoDto(visit.getCreatedBy()))
                .createdAt(visit.getCreatedAt())
                .updatedAt(visit.getUpdatedAt())
                .build();
    }

    /**
     * Build user info DTO
     */
    private VisitResponse.UserInfoDto buildUserInfoDto(User user) {
        return VisitResponse.UserInfoDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .matricule(user.getMatricule())
                .build();
    }

    /**
     * Build slot info DTO
     */
    private VisitResponse.SlotInfoDto buildSlotInfoDto(Slot slot) {
        return VisitResponse.SlotInfoDto.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus().name())
                .build();
    }

    // ==================== DOCTOR-SPECIFIC METHODS ====================

    /**
     * Get visits pending confirmation for a specific doctor
     *
     * @param doctorId Doctor ID
     * @return List of visits with PENDING_DOCTOR_CONFIRMATION status
     */
    public List<VisitResponse> getPendingConfirmationsForDoctor(Long doctorId) {
        log.info("Getting pending confirmations for doctor: {}", doctorId);

        List<Visit> visits = visitRepository.findByDoctorIdAndStatus(doctorId, VisitStatus.PENDING_DOCTOR_CONFIRMATION);

        log.info("Found {} pending confirmations for doctor: {}", visits.size(), doctorId);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get confirmed schedule for a specific doctor
     *
     * @param doctorId Doctor ID
     * @return List of visits with SCHEDULED status
     */
    public List<VisitResponse> getConfirmedScheduleForDoctor(Long doctorId) {
        log.info("Getting confirmed schedule for doctor: {}", doctorId);

        List<Visit> visits = visitRepository.findByDoctorIdAndStatus(doctorId, VisitStatus.SCHEDULED);

        log.info("Found {} scheduled visits for doctor: {}", visits.size(), doctorId);
        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Reject a visit (Doctor use)
     *
     * Changes visit status to CANCELLED
     * Releases the slot back to AVAILABLE
     *
     * @param visitId Visit ID to reject
     * @param doctor Doctor rejecting the visit
     * @return Updated visit response
     */
    @Transactional
    public VisitResponse rejectVisit(Long visitId, User doctor) {
        log.info("Doctor {} rejecting visit: {}", doctor.getEmail(), visitId);

        Visit visit = getVisitById(visitId);
        validateDoctorOwnership(visit, doctor);
        validateVisitCanBeRejected(visit);

        // Release the slot back to AVAILABLE
        Slot slot = visit.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setUpdatedAt(LocalDateTime.now());
        slotRepository.save(slot);

        // Update visit status to CANCELLED
        visit.setStatus(VisitStatus.CANCELLED);
        visit.setUpdatedAt(LocalDateTime.now());
        Visit savedVisit = visitRepository.save(visit);

        // Log the action
        logService.log(LogActionType.REFUSE_VISITE, "Visit rejected: ID=" + visitId + ", Doctor=" + doctor.getEmail());

        log.info("Visit rejected: ID={}, Doctor={}", visitId, doctor.getEmail());
        return buildVisitResponse(savedVisit);
    }

    /**
     * Update visit status (Doctor use)
     *
     * Allows doctors to update visit status (IN_PROGRESS, COMPLETED)
     *
     * @param visitId Visit ID to update
     * @param newStatus New visit status
     * @param doctor Doctor updating the status
     * @return Updated visit response
     */
    @Transactional
    public VisitResponse updateVisitStatus(Long visitId, VisitStatus newStatus, User doctor) {
        log.info("Doctor {} updating visit {} status to: {}", doctor.getEmail(), visitId, newStatus);

        Visit visit = getVisitById(visitId);
        validateDoctorOwnership(visit, doctor);
        validateStatusUpdateAllowed(visit, newStatus);

        // Update visit status
        visit.setStatus(newStatus);
        visit.setUpdatedAt(LocalDateTime.now());
        Visit savedVisit = visitRepository.save(visit);

        // Log the action
        logService.log(LogActionType.VALIDATE_VISITE, "Visit status updated: ID=" + visitId + ", Status=" + newStatus + ", Doctor=" + doctor.getEmail());

        log.info("Visit status updated: ID={}, Status={}, Doctor={}", visitId, newStatus, doctor.getEmail());
        return buildVisitResponse(savedVisit);
    }

    // ==================== ADDITIONAL VALIDATION METHODS ====================

    /**
     * Validate visit can be rejected
     */
    private void validateVisitCanBeRejected(Visit visit) {
        if (!VisitStatus.PENDING_DOCTOR_CONFIRMATION.equals(visit.getStatus())) {
            throw new IllegalStateException("Only pending visits can be rejected");
        }
    }

    /**
     * Validate status update is allowed
     */
    private void validateStatusUpdateAllowed(Visit visit, VisitStatus newStatus) {
        // Only allow specific status transitions
        if (VisitStatus.IN_PROGRESS.equals(newStatus)) {
            if (!VisitStatus.SCHEDULED.equals(visit.getStatus())) {
                throw new IllegalStateException("Only scheduled visits can be marked as in-progress");
            }
        } else if (VisitStatus.COMPLETED.equals(newStatus)) {
            if (!VisitStatus.IN_PROGRESS.equals(visit.getStatus())) {
                throw new IllegalStateException("Only in-progress visits can be marked as completed");
            }
        } else {
            throw new IllegalArgumentException("Invalid status transition to: " + newStatus);
        }
    }

    //  Inner class for user statistics
    @lombok.Builder
    @lombok.Data
    public static class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long archivedUsers;
        private long pendingUsers;
    }

    // ==================== HELPER METHODS FOR VISIT WITH SLOT ====================

    /**
     * Validate time constraints for visit with slot creation
     *
     * @param request Request to validate
     * @throws IllegalArgumentException if constraints are violated
     */
    private void validateTimeConstraints(CreateVisitWithSlotRequest request) {
        if (!request.isStartTimeInFuture()) {
            throw new IllegalArgumentException("Start time must be in the future");
        }

        if (!request.isTimeRangeValid()) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (!request.isDurationReasonable()) {
            throw new IllegalArgumentException("Duration must be between 15 minutes and 2 hours");
        }
    }

    /**
     * Validate no slot conflicts for the given time range
     *
     * @param doctor Doctor to check conflicts for
     * @param startTime Start time of new slot
     * @param endTime End time of new slot
     * @throws IllegalStateException if conflicts found
     */
    private void validateNoSlotConflicts(User doctor, LocalDateTime startTime, LocalDateTime endTime) {
        List<Slot> conflictingSlots = slotRepository.findConflictingSlots(doctor, startTime, endTime);

        if (!conflictingSlots.isEmpty()) {
            throw new IllegalStateException("Slot conflicts found with existing slots for doctor: " + doctor.getEmail());
        }
    }

    /**
     * Create slot entity from request
     *
     * @param request Request containing slot information
     * @param doctor Doctor for the slot
     * @return Created slot entity
     */
    private Slot createSlotEntity(CreateVisitWithSlotRequest request, User doctor) {
        return Slot.builder()
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(SlotStatus.TEMPORARILY_LOCKED) // Will be confirmed when doctor accepts
                .build();
    }

    /**
     * Create visit entity from request with slot
     *
     * @param request Request containing visit information
     * @param collaborator Collaborator for the visit
     * @param doctor Doctor for the visit
     * @param slot Slot for the visit
     * @param hrUser HR user creating the visit
     * @return Created visit entity
     */
    private Visit createVisitEntity(CreateVisitWithSlotRequest request, User collaborator, User doctor, Slot slot, User hrUser) {
        return Visit.builder()
                .collaborator(collaborator)
                .doctor(doctor)
                .slot(slot)
                .visitType(request.getVisitType())
                .status(VisitStatus.PENDING_DOCTOR_CONFIRMATION)
                .createdBy(hrUser)
                .build();
    }

    /**
     * Build response for visit with slot creation
     *
     * @param visit Created visit
     * @param slot Created slot
     * @return Visit with slot response
     */
    private VisitWithSlotResponse buildVisitWithSlotResponse(Visit visit, Slot slot) {
        return VisitWithSlotResponse.builder()
                .visit(VisitWithSlotResponse.VisitInfo.builder()
                        .id(visit.getId())
                        .collaboratorName(visit.getCollaborator().getFirstName() + " " + visit.getCollaborator().getLastName())
                        .doctorName(visit.getDoctor().getFirstName() + " " + visit.getDoctor().getLastName())
                        .visitType(visit.getVisitType())
                        .status(visit.getStatus())
                        .createdAt(visit.getCreatedAt())
                        .build())
                .slot(VisitWithSlotResponse.SlotInfo.builder()
                        .id(slot.getId())
                        .doctorName(slot.getDoctor().getFirstName() + " " + slot.getDoctor().getLastName())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .status(slot.getStatus().name())
                        .createdAt(slot.getCreatedAt())
                        .build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== COLLABORATOR ENDPOINTS ====================

    /**
     * Get all visits for a specific collaborator
     *
     * Used by collaborators to view their visit history
     *
     * @param collaboratorId ID of the collaborator
     * @return List of visits for the collaborator
     */
    public List<VisitResponse> getVisitsByCollaborator(Long collaboratorId) {
        log.info("Getting visits for collaborator: {}", collaboratorId);

        List<Visit> visits = visitRepository.findByCollaboratorId(collaboratorId);

        return visits.stream()
                .map(this::buildVisitResponse)
                .collect(Collectors.toList());
    }
}