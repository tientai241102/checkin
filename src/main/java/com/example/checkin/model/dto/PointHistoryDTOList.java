package com.example.checkin.model.dto;


import lombok.Data;

import java.util.List;

@Data
public  class PointHistoryDTOList {
    private List<PointHistoryDTO> pointHistoryList;
    private long totalElements;
}
