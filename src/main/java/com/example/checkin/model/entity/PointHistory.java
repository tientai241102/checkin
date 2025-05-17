package com.example.checkin.model.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "point_history")
@Data
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long points;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "total_points_after", nullable = false)
    private Long totalPointsAfter;

    @Column(name = "object_id")
    private Long objectId;
}