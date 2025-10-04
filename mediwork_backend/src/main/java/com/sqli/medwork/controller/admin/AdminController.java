package com.sqli.medwork.controller.admin;

import com.sqli.medwork.dto.request.AdminUserCreationRequest;
import com.sqli.medwork.dto.request.AdminUpdateUserRequest;
import com.sqli.medwork.dto.response.AdminReadUserResponse;
import com.sqli.medwork.dto.response.RoleCount;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.Role;
import com.sqli.medwork.enums.LogActionType;
import com.sqli.medwork.repository.UserRepository;
import com.sqli.medwork.service.common.LogService;
import com.sqli.medwork.service.user.UserService;
import com.sqli.medwork.exception.UserNotFoundException;
import com.sqli.medwork.exception.InvalidRoleException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LogService logService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignRole(
            @PathVariable Long id,
            @RequestBody String roleName) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        try {
            // ✅ FIXED: Remove quotes and handle role parsing properly
            String cleanRoleName = roleName.replace("\"", "").trim();
            Role newRole = Role.valueOf(cleanRoleName.toUpperCase());

            // ✅ ADDED: Validate that we're not assigning PENDING role
            if (newRole == Role.PENDING) {
                throw new InvalidRoleException("Cannot assign PENDING role - user must have a valid role");
            }

            Role oldRole = user.getRole();
            user.setRole(newRole);
            userRepository.save(user);

            logService.log(LogActionType.UPDATE_USER,
                    "Changement de rôle utilisateur " + user.getEmail() + " de " + oldRole + " à " + newRole);

            return ResponseEntity.ok("Rôle mis à jour avec succès pour l'utilisateur " + user.getEmail());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Nom de rôle invalide : " + roleName + ". Rôles valides : " +
                    String.join(", ", List.of("RH", "DOCTOR", "COLLABORATOR")));
        }
    }

    @PutMapping("/users/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> archiveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        user.setArchived(true);
        userRepository.save(user);
        logService.log(LogActionType.ARCHIVE_USER, "Utilisateur archivé : " + user.getEmail());

        return ResponseEntity.ok("Utilisateur archivé avec succès");
    }

    @PutMapping("/users/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> restoreUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        user.setArchived(false);
        userRepository.save(user);
        logService.log(LogActionType.RESTORE_USER, "Utilisateur restauré : " + user.getEmail());

        return ResponseEntity.ok("Utilisateur restauré avec succès");
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUserByAdmin(@Valid @RequestBody AdminUserCreationRequest request) {
        userService.createUserByAdmin(request);
        return ResponseEntity.ok("Utilisateur créé avec succès");
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserByAdmin(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest request) {
        userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok("Utilisateur mis à jour avec succès");
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminReadUserResponse> getUserDetails(@PathVariable Long id) {
        AdminReadUserResponse user = userService.getUserDetailsById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/rh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getRHUsers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.RH));
    }

    @GetMapping("/users/medecin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getMedecinUsers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.DOCTOR));
    }

    @GetMapping("/users/collaborateur")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getCollaborateurUsers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.COLLABORATOR));
    }

    @GetMapping("/users/count-by-role")
    public List<RoleCount> getUserCountByRole() {
        return userService.getUserCountByRole();
    }
}