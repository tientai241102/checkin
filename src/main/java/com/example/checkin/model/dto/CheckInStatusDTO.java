package com.example.checkin.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CheckInStatusDTO {
    private LocalDate date;
    private boolean checkedIn;
    private Integer checkInSequence;
    private Long points;
    private Long totalPointsAfter;
}