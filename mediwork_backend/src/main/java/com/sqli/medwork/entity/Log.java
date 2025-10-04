package com.sqli.medwork.entity;
import com.sqli.medwork.enums.LogActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String performedBy; // e.g. john@example.com

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 64, nullable = false)
    private LogActionType actionType;

    private String role; // e.g. ADMIN, RH, MEDECIN

    private String description; // Human-readable message

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}