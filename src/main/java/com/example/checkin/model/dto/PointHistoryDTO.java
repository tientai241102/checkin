package com.example.checkin.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PointHistoryDTO {
    private Long id;
    private Long points;
    private String transactionType;
    private Date createdAt;
    private Long totalPointsAfter;
    private Long objectId;
}