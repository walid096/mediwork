package com.sqli.medwork.service.user;

import com.sqli.medwork.dto.request.AdminUserCreationRequest;
import com.sqli.medwork.dto.request.AdminUpdateUserRequest;
import com.sqli.medwork.dto.response.AdminReadUserResponse;
import com.sqli.medwork.dto.response.RoleCount;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.exception.EmailAlreadyExistsException;
import com.sqli.medwork.exception.UserNotFoundException;
import com.sqli.medwork.exception.MatriculeAlreadyExistsException;
import com.sqli.medwork.exception.InvalidRoleException;
import com.sqli.medwork.service.common.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import com.sqli.medwork.dto.response.DoctorInfoResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    /**
     * Create new user by admin
     * @param request User creation request
     */
    @Transactional
    public void createUserByAdmin(AdminUserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email déjà utilisé");
        }
        if (userRepository.existsByMatricule(request.getMatricule())) {
            throw new MatriculeAlreadyExistsException("Matricule déjà utilisé");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMatricule(request.getMatricule());
        user.setRole(request.getRole());
        user.setArchived(false);

        userRepository.save(user);

        logService.log(LogActionType.CREATE_USER, "Utilisateur créé par admin : " + request.getEmail());
    }

    /**
     * Update user by admin
     * @param id User ID
     * @param request User update request
     */
    @Transactional
    public void updateUserByAdmin(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        // Email validation
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("Email déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }

        // Matricule validation
        if (request.getMatricule() != null && !request.getMatricule().equals(user.getMatricule())) {
            if (userRepository.findAll().stream().anyMatch(u -> request.getMatricule().equals(u.getMatricule()) && !u.getId().equals(id))) {
                throw new MatriculeAlreadyExistsException("Matricule déjà utilisé");
            }
            user.setMatricule(request.getMatricule());
        }

        // First name update
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        // Last name update
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Password update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Role update
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        userRepository.save(user);
        logService.log(LogActionType.UPDATE_USER, "Utilisateur mis à jour par admin : " + user.getEmail());
    }

    /**
     * Get all users with PENDING role
     * @return List of pending users
     */
    public List<User> getPendingUsers() {
        return userRepository.findByRole(Role.PENDING);
    }

    /**
     * Assign role to user
     * @param id User ID
     * @param role New role to assign
     */
    @Transactional
    public void assignRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        // Validate role assignment
        if (role == Role.PENDING) {
            throw new InvalidRoleException("Cannot assign PENDING role - user must have a valid role");
        }

        Role oldRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);

        logService.log(LogActionType.UPDATE_USER,
                "Rôle attribué à l'utilisateur " + user.getEmail() + " : " + oldRole + " → " + role);
    }

    /**
     * Get user details by ID (DTO response)
     * @param id User ID
     * @return User details DTO
     */
    public AdminReadUserResponse getUserDetailsById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        AdminReadUserResponse dto = new AdminReadUserResponse();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMatricule(user.getMatricule());
        dto.setRole(user.getRole());
        dto.setArchived(user.isArchived());
        dto.setDateCreation(user.getDateCreation() != null ? user.getDateCreation().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

        return dto;
    }

    /**
     * Get user count by role
     * @return List of role counts
     */
    public List<RoleCount> getUserCountByRole() {
        return userRepository.countUsersByRole();
    }

    // ✅ ADDED: Get user by ID (for admin use)
    /**
     * Get user by ID
     * @param id User ID
     * @return User entity
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
    }

    // ✅ ADDED: Get users by role (for admin use)
    /**
     * Get users by specific role
     * @param role Role to filter by
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // ✅ ADDED: Archive user (for admin use)
    /**
     * Archive a user
     * @param id User ID to archive
     */
    @Transactional
    public void archiveUser(Long id) {
        User user = getUserById(id);
        user.setArchived(true);
        userRepository.save(user);
        logService.log(LogActionType.ARCHIVE_USER, "Utilisateur archivé : " + user.getEmail());
    }

    // ✅ ADDED: Restore user (for admin use)
    /**
     * Restore an archived user
     * @param id User ID to restore
     */
    @Transactional
    public void restoreUser(Long id) {
        User user = getUserById(id);
        user.setArchived(false);
        userRepository.save(user);
        logService.log(LogActionType.RESTORE_USER, "Utilisateur restauré : " + user.getEmail());
    }

    // ✅ ADDED: Get all users (for admin use)
    /**
     * Get all users (admin only)
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ ADDED: Get active users only (for admin use)
    /**
     * Get all active (non-archived) users
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isArchived())
                .toList();
    }

    // ✅ ADDED: Search users by criteria (for admin use)
    /**
     * Search users by email or name
     * @param searchTerm Search term
     * @return List of matching users
     */
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        String term = searchTerm.toLowerCase().trim();
        return userRepository.findAll().stream()
                .filter(user ->
                        user.getEmail().toLowerCase().contains(term) ||
                                user.getFirstName().toLowerCase().contains(term) ||
                                user.getLastName().toLowerCase().contains(term) ||
                                user.getMatricule().toLowerCase().contains(term)
                )
                .toList();
    }

    // ✅ ADDED: Validate user role assignment (for admin use)
    /**
     * Validate if a role assignment is allowed
     * @param currentRole Current user role
     * @param newRole New role to assign
     * @return true if assignment is valid
     */
    public boolean isValidRoleAssignment(Role currentRole, Role newRole) {
        // Cannot assign PENDING role
        if (newRole == Role.PENDING) {
            return false;
        }

        // Cannot change ADMIN role (security measure)
        if (currentRole == Role.ADMIN && newRole != Role.ADMIN) {
            return false;
        }

        return true;
    }

    // ✅ ADDED: Get user by email (for controller use)
    /**
     * Get user by email
     * @param email User email
     * @return User entity
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé: " + email));
    }

    // ✅ ADDED: Get active doctors (for RH use)
    /**
     * Get all active doctors
     * 
     * Used by RH users to:
     * - View available doctors when scheduling visits
     * - Check doctor availability
     * - Manage doctor assignments
     * 
     * @return List of active doctors
     */
    public List<User> getActiveDoctors() {
        return userRepository.findByRole(Role.DOCTOR).stream()
                .filter(user -> !user.isArchived())
                .toList();
    }

    // ✅ ADDED: Get active doctors as DTOs (for RH use)
    /**
     * Get all active doctors as DTOs
     * 
     * Returns doctor information without sensitive data like password
     * Used by RH users for visit scheduling
     * 
     * @return List of doctor info DTOs
     */
    public List<DoctorInfoResponse> getActiveDoctorsAsDTOs() {
        return getActiveDoctors().stream()
                .map(this::convertToDoctorInfoResponse)
                .toList();
    }

    // ✅ ADDED: Convert User to DoctorInfoResponse
    /**
     * Convert User entity to DoctorInfoResponse DTO
     * 
     * @param user User entity
     * @return DoctorInfoResponse DTO
     */
    private DoctorInfoResponse convertToDoctorInfoResponse(User user) {
        return DoctorInfoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .matricule(user.getMatricule())
                .dateCreation(user.getDateCreation())
                .lastLogin(user.getLastLogin())
                .archived(user.isArchived())
                .build();
    }

    // ✅ ADDED: Get active collaborators (for RH use)
    /**
     * Get all active collaborators
     * 
     * Used by RH users to:
     * - View available collaborators when scheduling visits
     * - Check collaborator information
     * - Manage visit assignments
     * 
     * @return List of active collaborators
     */
    public List<User> getActiveCollaborators() {
        return userRepository.findByRole(Role.COLLABORATOR).stream()
                .filter(user -> !user.isArchived())
                .toList();
    }

    // ✅ ADDED: Get active collaborators as DTOs (for RH use)
    /**
     * Get all active collaborators as DTOs
     * 
     * Returns collaborator information without sensitive data like password
     * Used by RH users for visit scheduling
     * 
     * @return List of collaborator info DTOs
     */
    public List<DoctorInfoResponse> getActiveCollaboratorsAsDTOs() {
        return getActiveCollaborators().stream()
                .map(this::convertToDoctorInfoResponse)
                .toList();
    }

    // ✅ ADDED: Get doctor by ID as DTO (for RH use)
    /**
     * Get doctor by ID as DTO
     * 
     * Used by RH users to get specific doctor information
     * 
     * @param doctorId Doctor ID
     * @return Doctor info DTO
     * @throws UserNotFoundException if doctor not found
     * @throws IllegalArgumentException if user is not a doctor
     */
    public DoctorInfoResponse getDoctorByIdAsDTO(Long doctorId) {
        User user = getUserById(doctorId);
        
        // Verify the user is a doctor
        if (user.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("User with ID " + doctorId + " is not a doctor");
        }
        
        // Verify the doctor is not archived
        if (user.isArchived()) {
            throw new IllegalArgumentException("Doctor with ID " + doctorId + " is archived");
        }
        
        return convertToDoctorInfoResponse(user);
    }

    // ✅ ADDED: Get user statistics (for admin dashboard)
    /**
     * Get user statistics for admin dashboard
     * @return User statistics
     */
    public UserStatistics getUserStatistics() {
        List<User> allUsers = getAllUsers();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(user -> !user.isArchived()).count();
        long archivedUsers = totalUsers - activeUsers;
        long pendingUsers = allUsers.stream().filter(user -> user.getRole() == Role.PENDING).count();

        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .archivedUsers(archivedUsers)
                .pendingUsers(pendingUsers)
                .build();
    }

    // ✅ ADDED: Get my profile as DTO (for collaborator use)
    /**
     * Get current user's profile as DTO
     * 
     * Used by collaborators to view their own profile information
     * 
     * @param email User's email
     * @return User profile info DTO
     * @throws UserNotFoundException if user not found
     */
    public DoctorInfoResponse getMyProfileAsDTO(String email) {
        User user = getUserByEmail(email);
        
        // Verify the user is not archived
        if (user.isArchived()) {
            throw new IllegalArgumentException("User account is archived");
        }
        
        return convertToDoctorInfoResponse(user);
    }

    // ✅ ADDED: Inner class for user statistics
    @lombok.Builder
    @lombok.Data
    public static class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long archivedUsers;
        private long pendingUsers;
    }
}