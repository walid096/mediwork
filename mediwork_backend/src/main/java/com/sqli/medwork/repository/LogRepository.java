package com.sqli.medwork.repository;

import com.sqli.medwork.entity.Log;
import com.sqli.medwork.enums.LogActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    // ✅ Basic filtering methods (Spring implements automatically)
    List<Log> findByActionType(LogActionType actionType);
    List<Log> findByPerformedBy(String email);
    List<Log> findByRole(String role);
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}