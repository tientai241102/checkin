package com.example.checkin.repository;


import com.example.checkin.model.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    @Query("SELECT  p FROM PointHistory p WHERE p.userId = :userId and p.transactionType =:type")
    Page<PointHistory> findByUserId(Long userId, String type, Pageable pageable);
}