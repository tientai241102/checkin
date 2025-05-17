package com.example.checkin.model.mapper;

import com.example.checkin.model.dto.PointHistoryDTO;
import com.example.checkin.model.entity.PointHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PointHistoryMapper extends BaseMapper<PointHistoryDTO, PointHistory>{
}
