package com.example.checkin.repository;

import com.example.checkin.model.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByUserIdAndCheckInMonth(Long userId, String checkInMonth);
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
}