package com.sqli.medwork.service.visit;

import com.sqli.medwork.dto.request.SpontaneousVisitRequest;
import com.sqli.medwork.dto.response.SpontaneousVisitResponse;
import com.sqli.medwork.dto.request.UpdateSpontaneousVisitRequest;
import com.sqli.medwork.exception.ApiException;
import org.springframework.http.HttpStatus;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.SchedulingStatus;
import com.sqli.medwork.exception.UserNotFoundException;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.repository.SpontaneousVisitDetailsRepository;
import com.sqli.medwork.entity.SpontaneousVisitDetails;
import com.sqli.medwork.repository.RecurringSlotRepository;
import com.sqli.medwork.repository.SlotRepository;
import com.sqli.medwork.repository.LogRepository;
import com.sqli.medwork.entity.Log;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.entity.RecurringSlot;
import com.sqli.medwork.entity.Slot;
import com.sqli.medwork.enums.VisitType;
import com.sqli.medwork.dto.request.CreateVisitWithSlotRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for managing spontaneous visit requests (detached from Visit)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SpontaneousVisitService {

    private final UserRepository userRepository;
    private final SpontaneousVisitDetailsRepository spontaneousVisitDetailsRepository;
    private final RecurringSlotRepository recurringSlotRepository;
    private final SlotRepository slotRepository;
    private final VisitService visitService;
    private final LogRepository logRepository;

    /**
     * Create a new spontaneous visit request
     */
    public SpontaneousVisitResponse createSpontaneousVisit(SpontaneousVisitRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        SpontaneousVisitDetails details = SpontaneousVisitDetails.builder()
            .collaborator(collaborator)
            .reason(request.getReason())
            .additionalNotes(request.getAdditionalNotes())
            .preferredDateTime(request.getPreferredDateTime())
            .schedulingStatus(SchedulingStatus.PENDING)
            .build();

        spontaneousVisitDetailsRepository.save(details);

        return mapToResponse(details);
    }

    /**
     * Get all spontaneous visit requests for the authenticated collaborator
     */
    public List<SpontaneousVisitResponse> getMySpontaneousVisits(Authentication authentication) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return spontaneousVisitDetailsRepository.findByCollaborator(collaborator)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get spontaneous visit requests for the authenticated collaborator with filters
     */
    public List<SpontaneousVisitResponse> getMySpontaneousVisitsWithFilters(
            Authentication authentication,
            SchedulingStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return spontaneousVisitDetailsRepository.findByCollaboratorWithFilters(collaborator, status, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get count of spontaneous visit requests by status for the authenticated collaborator
     */
    public long getMyRequestsCountByStatus(Authentication authentication, SchedulingStatus status) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return spontaneousVisitDetailsRepository.countByCollaboratorAndSchedulingStatus(collaborator, status);
    }

    /**
     * Get a specific spontaneous visit request by ID
     */
    public SpontaneousVisitResponse getSpontaneousVisit(Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

        if (!details.getCollaborator().getId().equals(collaborator.getId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à consulter cette demande");
        }

        return mapToResponse(details);
    }

    /**
     * Update a spontaneous visit request
     */
    public SpontaneousVisitResponse updateSpontaneousVisit(Long id, UpdateSpontaneousVisitRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

        if (!details.getCollaborator().getId().equals(collaborator.getId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        // Only allow updates if still pending scheduling
        if (details.getSchedulingStatus() != SchedulingStatus.PENDING) {
            throw new IllegalStateException("Impossible de modifier une demande qui n'est plus en attente");
        }

        if (request.getReason() != null) details.setReason(request.getReason());
        if (request.getAdditionalNotes() != null) details.setAdditionalNotes(request.getAdditionalNotes());
        if (request.getPreferredDateTime() != null) details.setPreferredDateTime(request.getPreferredDateTime());

        spontaneousVisitDetailsRepository.save(details);

        return mapToResponse(details);
    }

    /**
     * Cancel a spontaneous visit request
     */
    public void cancelSpontaneousVisit(Long id, String cancellationReason, Authentication authentication) {
        String userEmail = authentication.getName();
        User collaborator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

        if (!details.getCollaborator().getId().equals(collaborator.getId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à annuler cette demande");
        }

        if (details.getSchedulingStatus() != SchedulingStatus.PENDING) {
            throw new IllegalStateException("Impossible d'annuler une demande qui n'est plus en attente");
        }

        details.setSchedulingStatus(SchedulingStatus.CANCELLED);
        spontaneousVisitDetailsRepository.save(details);

    // Log the cancellation with optional reason
    String desc = String.format("Collaborateur %s a annulé la demande #%d%s", 
        userEmail, id, 
        (cancellationReason != null && !cancellationReason.isBlank()) ? 
        String.format(" (raison: %s)", cancellationReason) : "");
    Log logEntry = Log.builder()
        .performedBy(userEmail)
        .role("COLLABORATOR")
        .actionType(LogActionType.CANCEL_VISITE)
        .description(desc)
        .build();
    logRepository.save(logEntry);
    }

    /**
     * RH/ADMIN cancellation of a spontaneous visit request.
     * Allowed unless already SCHEDULED. Records a log with the reason.
     */
    public void cancelSpontaneousVisitByAdmin(Long id, String cancellationReason, Authentication authentication) {
    String userEmail = authentication.getName();

    SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

    if (details.getSchedulingStatus() == SchedulingStatus.SCHEDULED) {
        throw new IllegalStateException("Impossible d'annuler une demande déjà planifiée");
    }

    if (details.getSchedulingStatus() == SchedulingStatus.CANCELLED) {
        // idempotent: just log and return
        String descAlready = String.format("RH/ADMIN %s a tenté d'annuler la demande #%d déjà annulée%s",
            userEmail, id,
            (cancellationReason != null && !cancellationReason.isBlank()) ?
                String.format(" (raison: %s)", cancellationReason) : "");
        logRepository.save(Log.builder()
            .performedBy(userEmail)
            .role("RH/ADMIN")
            .actionType(LogActionType.CANCEL_VISITE)
            .description(descAlready)
            .build());
        return;
    }

    details.setSchedulingStatus(SchedulingStatus.CANCELLED);
    spontaneousVisitDetailsRepository.save(details);

    String desc = String.format("RH/ADMIN %s a annulé la demande #%d%s",
        userEmail, id,
        (cancellationReason != null && !cancellationReason.isBlank()) ?
            String.format(" (raison: %s)", cancellationReason) : "");
    logRepository.save(Log.builder()
        .performedBy(userEmail)
        .role("RH/ADMIN")
        .actionType(LogActionType.CANCEL_VISITE)
        .description(desc)
        .build());
    }

    /**
     * Get all spontaneous visit requests (for HR/Admin use)
     */
    public List<SpontaneousVisitResponse> getAllSpontaneousVisits() {
        return spontaneousVisitDetailsRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(SpontaneousVisitDetails::getCreatedAt).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Confirm a spontaneous visit request. If a new date is provided, update it first.
    * Only RH/Admin should call this from controller-level security.
     */
    public SpontaneousVisitResponse confirmSpontaneousVisit(
            Long id,
            LocalDateTime newDateTime,
            Long doctorId,
            VisitType visitType,
            Authentication authentication) {

        SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

        if (details.getSchedulingStatus() == SchedulingStatus.CANCELLED) {
            throw new ApiException("Impossible de confirmer une demande annulée", HttpStatus.BAD_REQUEST, "INVALID_STATE");
        }

        // Determine target date/time
        LocalDateTime targetDateTime = (newDateTime != null) ? newDateTime : details.getPreferredDateTime();
        if (targetDateTime == null) {
            throw new ApiException("Aucune date fournie pour la confirmation", HttpStatus.BAD_REQUEST, "DATE_REQUIRED");
        }
        
        // Add a small buffer (5 minutes) to account for timezone differences and processing time
        LocalDateTime nowWithBuffer = LocalDateTime.now().minusMinutes(5);
        if (targetDateTime.isBefore(nowWithBuffer)) {
            log.warn("Target time {} is before current time with buffer {}", targetDateTime, nowWithBuffer);
            throw new ApiException("La date doit être dans le futur", HttpStatus.BAD_REQUEST, "INVALID_DATE");
        }

        // Validate doctor
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Médecin introuvable"));
        if (doctor.getRole() == null || doctor.getRole() != com.sqli.medwork.enums.Role.DOCTOR) {
            throw new ApiException("L'utilisateur sélectionné n'est pas un médecin", HttpStatus.BAD_REQUEST, "INVALID_DOCTOR");
        }

        // Find matching recurring slot on the given day/time
        DayOfWeek day = targetDateTime.getDayOfWeek();
        LocalTime time = targetDateTime.toLocalTime();
        List<RecurringSlot> daySlots = recurringSlotRepository.findByDoctorAndDayOfWeekOrderByStartTimeAsc(doctor, day);

        // Debug logging
        log.info("Looking for slot on {} at {} for doctor {}", day, time, doctor.getEmail());
        log.info("Found {} recurring slots for this day", daySlots.size());
        for (RecurringSlot slot : daySlots) {
            log.info("Slot: {} - {} (checking if {} is within this range)", 
                slot.getStartTime(), slot.getEndTime(), time);
        }

        RecurringSlot matching = daySlots.stream()
                .filter(rs -> {
                    // Time should be >= startTime and <= endTime (inclusive of both ends)
                    boolean isWithinRange = !time.isBefore(rs.getStartTime()) && !time.isAfter(rs.getEndTime());
                    log.info("Checking slot {} - {}: time {} is within range = {}", 
                        rs.getStartTime(), rs.getEndTime(), time, isWithinRange);
                    return isWithinRange;
                })
                .findFirst()
                .orElse(null);

        if (matching == null) {
            String availableSlots = daySlots.stream()
                .map(rs -> rs.getStartTime() + " - " + rs.getEndTime())
                .collect(Collectors.joining(", "));
            throw new ApiException(
                String.format("Le médecin n'est pas disponible à cette heure (%s sur %s). Créneaux disponibles: [%s]", 
                    time, day, availableSlots), 
                HttpStatus.BAD_REQUEST, "NO_RECURRING_SLOT");
        }

        // Build concrete slot time for the selected date - 1 hour duration starting at target time
        LocalDateTime startTime = targetDateTime.withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        // Verify the 1-hour slot fits within the recurring slot window
        LocalTime recurringStart = matching.getStartTime();
        LocalTime recurringEnd = matching.getEndTime();
        LocalTime slotStart = startTime.toLocalTime();
        LocalTime slotEnd = endTime.toLocalTime();

        if (slotStart.isBefore(recurringStart) || slotEnd.isAfter(recurringEnd)) {
            throw new ApiException(
                String.format("Le créneau d'1 heure (%s - %s) ne s'adapte pas dans la disponibilité du médecin (%s - %s)", 
                    slotStart, slotEnd, recurringStart, recurringEnd), 
                HttpStatus.BAD_REQUEST, "SLOT_OUTSIDE_RECURRING_WINDOW");
        }

        log.info("Creating 1-hour slot: {} - {} within recurring window {} - {}", 
            startTime, endTime, recurringStart, recurringEnd);

        if (!startTime.isBefore(endTime)) {
            throw new ApiException("Plage horaire invalide pour la date sélectionnée", HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE");
        }

        // Check if doctor already has a slot at this time to prevent conflicts
        List<Slot> existingSlots = slotRepository.findConflictingSlots(doctor, startTime, endTime);
        if (!existingSlots.isEmpty()) {
            String conflictDetails = existingSlots.stream()
                .map(slot -> String.format("%s - %s (Status: %s)", 
                    slot.getStartTime().toLocalTime(), 
                    slot.getEndTime().toLocalTime(), 
                    slot.getStatus().name()))
                .collect(Collectors.joining(", "));
            
            throw new ApiException(
                String.format("Le médecin %s %s a déjà un créneau réservé à cette heure. Créneaux en conflit: [%s]", 
                    doctor.getFirstName(), doctor.getLastName(), conflictDetails), 
                HttpStatus.CONFLICT, "DOCTOR_SLOT_CONFLICT");
        }

        // Create visit with new slot via VisitService (handles conflicts and validations)
        User hrUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        CreateVisitWithSlotRequest req = CreateVisitWithSlotRequest.builder()
                .collaboratorId(details.getCollaborator().getId())
                .doctorId(doctor.getId())
                .visitType(visitType != null ? visitType : VisitType.SPONTANEOUS)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        try {
            // Will throw if conflicts / invalid (but we already checked conflicts above)
            visitService.createVisitWithSlot(req, hrUser);
        } catch (IllegalStateException e) {
            // If the error is about slot conflicts, we already handled it above
            // This catch is for any other validation errors from VisitService
            if (e.getMessage().contains("Slot conflicts found")) {
                // This shouldn't happen since we already checked, but if it does, use our better error message
                throw new ApiException(
                    String.format("Le médecin %s %s a déjà un créneau réservé à cette heure.", 
                        doctor.getFirstName(), doctor.getLastName()), 
                    HttpStatus.CONFLICT, "DOCTOR_SLOT_CONFLICT");
            }
            // Re-throw other validation errors as-is
            throw e;
        }

        // Update and persist spontaneous request as scheduled
        details.setPreferredDateTime(targetDateTime);
        details.setSchedulingStatus(SchedulingStatus.SCHEDULED);
        spontaneousVisitDetailsRepository.save(details);

        return mapToResponse(details);
    }

    /**
     * Reject a spontaneous visit request (sets status to CANCELLED).
     * Only RH/Admin should call this from controller-level security.
     */
    public SpontaneousVisitResponse rejectSpontaneousVisit(Long id) {
        SpontaneousVisitDetails details = spontaneousVisitDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Demande de visite non trouvée"));

        if (details.getSchedulingStatus() == SchedulingStatus.CANCELLED) {
            throw new ApiException("La demande est déjà annulée", HttpStatus.BAD_REQUEST, "ALREADY_CANCELLED");
        }
        if (details.getSchedulingStatus() == SchedulingStatus.SCHEDULED) {
            throw new ApiException("Impossible de rejeter une demande déjà planifiée", HttpStatus.BAD_REQUEST, "INVALID_STATE");
        }

        details.setSchedulingStatus(SchedulingStatus.CANCELLED);
        spontaneousVisitDetailsRepository.save(details);
        return mapToResponse(details);
    }

    private SpontaneousVisitResponse mapToResponse(SpontaneousVisitDetails details) {
        return SpontaneousVisitResponse.builder()
                .id(details.getId())
                .reason(details.getReason())
                .additionalNotes(details.getAdditionalNotes())
                .collaboratorName(details.getCollaborator().getFirstName() + " " + details.getCollaborator().getLastName())
                .createdAt(details.getCreatedAt())
                .updatedAt(details.getUpdatedAt())
                .preferredDateTime(details.getPreferredDateTime())
                .schedulingStatus(details.getSchedulingStatus())
                .build();
    }
}