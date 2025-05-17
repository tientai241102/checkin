package com.example.checkin.model.mapper;

import com.example.checkin.model.dto.PointHistoryDTO;
import com.example.checkin.model.entity.PointHistory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-17T23:17:27+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.8 (Amazon.com Inc.)"
)
@Component
public class PointHistoryMapperImpl implements PointHistoryMapper {

    @Override
    public PointHistory requestToResponse(PointHistoryDTO req) {
        if ( req == null ) {
            return null;
        }

        PointHistory pointHistory = new PointHistory();

        pointHistory.setId( req.getId() );
        pointHistory.setPoints( req.getPoints() );
        pointHistory.setTransactionType( req.getTransactionType() );
        pointHistory.setCreatedAt( req.getCreatedAt() );
        pointHistory.setTotalPointsAfter( req.getTotalPointsAfter() );
        pointHistory.setObjectId( req.getObjectId() );

        return pointHistory;
    }

    @Override
    public PointHistoryDTO responseToRequest(PointHistory req) {
        if ( req == null ) {
            return null;
        }

        PointHistoryDTO pointHistoryDTO = new PointHistoryDTO();

        pointHistoryDTO.setId( req.getId() );
        pointHistoryDTO.setPoints( req.getPoints() );
        pointHistoryDTO.setTransactionType( req.getTransactionType() );
        pointHistoryDTO.setCreatedAt( req.getCreatedAt() );
        pointHistoryDTO.setTotalPointsAfter( req.getTotalPointsAfter() );
        pointHistoryDTO.setObjectId( req.getObjectId() );

        return pointHistoryDTO;
    }

    @Override
    public List<PointHistory> toEntityList(List<PointHistoryDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<PointHistory> list = new ArrayList<PointHistory>( dtos.size() );
        for ( PointHistoryDTO pointHistoryDTO : dtos ) {
            list.add( requestToResponse( pointHistoryDTO ) );
        }

        return list;
    }

    @Override
    public List<PointHistoryDTO> toDTOList(List<PointHistory> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PointHistoryDTO> list = new ArrayList<PointHistoryDTO>( entities.size() );
        for ( PointHistory pointHistory : entities ) {
            list.add( responseToRequest( pointHistory ) );
        }

        return list;
    }
}
