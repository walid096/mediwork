package com.sqli.medwork.repository;

import com.sqli.medwork.dto.response.RoleCount;
import com.sqli.medwork.entity.User;
import com.sqli.medwork.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);  // ← Uncomment this!
    List<User> findByRole(Role role);

    @Query("SELECT u.role AS role, COUNT(u) AS count FROM User u GROUP BY u.role")
    List<RoleCount> countUsersByRole();
}