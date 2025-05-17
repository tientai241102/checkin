package com.example.checkin.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "check_ins")
@Data
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_in_date", nullable = false)
    private Date checkInDate;

    @Column(name = "check_in_month", nullable = false)
    private String checkInMonth;

    @Column(name = "check_in_sequence", nullable = false)
    private Integer checkInSequence;

    @Column(nullable = false)
    private Long points;

    @Column(name = "total_points_after", nullable = false)
    private Long totalPointsAfter;
}