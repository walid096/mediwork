package com.sqli.medwork.repository;

import com.sqli.medwork.entity.RecurringSlot;
import com.sqli.medwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringSlotRepository extends JpaRepository<RecurringSlot, Long> {

    // Find all recurring slots for a specific doctor
    List<RecurringSlot> findByDoctorOrderByDayOfWeekAscStartTimeAsc(User doctor);

    // Find recurring slots by doctor and day of week
    List<RecurringSlot> findByDoctorAndDayOfWeekOrderByStartTimeAsc(User doctor, DayOfWeek dayOfWeek);

    // Check if doctor already has a recurring slot for a specific day and time range
    @Query("SELECT rs FROM RecurringSlot rs WHERE rs.doctor = :doctor AND rs.dayOfWeek = :dayOfWeek " +
            "AND ((rs.startTime < :endTime AND rs.endTime > :startTime))")
    List<RecurringSlot> findOverlappingSlots(
            @Param("doctor") User doctor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime
    );

    // Find recurring slot by ID and doctor (for security)
    Optional<RecurringSlot> findByIdAndDoctor(Long id, User doctor);

    // Delete all recurring slots for a doctor
    void deleteByDoctor(User doctor);
}