package com.example.checkin.service;

import com.example.checkin.model.dto.CheckInStatusDTO;
import com.example.checkin.model.dto.PointHistoryDTO;
import com.example.checkin.model.request.DeductPointsRequest;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface CheckInService {
    public void checkIn(Long userId);
    List<CheckInStatusDTO> getCheckInStatus(Long userId);
    public void deductPoints(Long userId, DeductPointsRequest request);
    Page<PointHistoryDTO> getPointHistory(Long userId, int page, int size);

}
