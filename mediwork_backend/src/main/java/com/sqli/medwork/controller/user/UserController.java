package com.sqli.medwork.controller.user;

import com.sqli.medwork.dto.response.DoctorInfoResponse;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDetails getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;
    }

    /**
     * Get collaborator profile information
     * 
     * Used by collaborators to:
     * - View their profile information
     * - Check their personal details
     * 
     * @param userDetails Authenticated user details
     * @return Collaborator profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('COLLABORATOR')")
    public ResponseEntity<DoctorInfoResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            DoctorInfoResponse profile = userService.getMyProfileAsDTO(userDetails.getUsername());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error retrieving profile for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ ADDED: Test endpoint to verify authentication and role
    /**
     * Test endpoint to verify user authentication and role
     * 
     * Used for debugging authentication issues
     * 
     * @param userDetails Authenticated user details
     * @return User authentication information
     */
    @GetMapping("/test-auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        String authorities = userDetails.getAuthorities().toString();
        
        log.info("User {} testing authentication. Authorities: {}", username, authorities);
        
        return ResponseEntity.ok("Authenticated as: " + username + " with roles: " + authorities);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }

    @PutMapping("/{id}/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable Long id, @RequestBody Role role) {
        userService.assignRole(id, role);
        return ResponseEntity.ok("Rôle attribué avec succès");
    }

    // ==================== RH ENDPOINTS ====================

    /**
     * Get all active doctors (RH use)
     * 
     * Used by RH users to:
     * - View available doctors when scheduling visits
     * - Check doctor availability
     * - Manage doctor assignments
     * 
     * @return List of active doctors
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<DoctorInfoResponse>> getActiveDoctors() {
        
        log.info("RH/Admin requesting list of active doctors");
        
        try {
            List<DoctorInfoResponse> doctors = userService.getActiveDoctorsAsDTOs();
            log.info("Found {} active doctors", doctors.size());
            return ResponseEntity.ok(doctors);
            
        } catch (Exception e) {
            log.error("Error retrieving active doctors: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get doctors with basic information for RH use
     * 
     * Returns doctors with essential information needed for visit scheduling:
     * - ID, name, email, matricule
     * - Excludes sensitive information like password
     * 
     * @return List of doctors with basic information
     */
    @GetMapping("/doctors/basic-info")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<DoctorInfoResponse>> getDoctorsBasicInfo() {
        
        log.info("RH/Admin requesting basic doctor information");
        
        try {
            List<DoctorInfoResponse> doctors = userService.getActiveDoctorsAsDTOs();
            log.info("Returning basic info for {} doctors", doctors.size());
            return ResponseEntity.ok(doctors);
            
        } catch (Exception e) {
            log.error("Error retrieving doctor basic info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all active collaborators (RH use)
     * 
     * Used by RH users to:
     * - View available collaborators when scheduling visits
     * - Check collaborator information
     * - Manage visit assignments
     * 
     * @return List of active collaborators
     */
    @GetMapping("/collaborators")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<List<DoctorInfoResponse>> getActiveCollaborators() {
        
        log.info("RH/Admin requesting list of active collaborators");
        
        try {
            List<DoctorInfoResponse> collaborators = userService.getActiveCollaboratorsAsDTOs();
            log.info("Found {} active collaborators", collaborators.size());
            return ResponseEntity.ok(collaborators);
            
        } catch (Exception e) {
            log.error("Error retrieving active collaborators: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get doctor by ID (RH use)
     * 
     * Used by RH users to:
     * - Get specific doctor information when scheduling visits
     * - Verify doctor details
     * - Check doctor availability
     * 
     * @param doctorId Doctor ID
     * @return Doctor information
     */
    @GetMapping("/doctors/{doctorId}")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<DoctorInfoResponse> getDoctorById(@PathVariable Long doctorId) {
        
        log.info("RH/Admin requesting doctor information for ID: {}", doctorId);
        
        try {
            DoctorInfoResponse doctor = userService.getDoctorByIdAsDTO(doctorId);
            log.info("Found doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
            return ResponseEntity.ok(doctor);
            
        } catch (Exception e) {
            log.error("Error retrieving doctor with ID {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}




