package com.example.checkin.model.mapper;

import org.mapstruct.Mapper;

import java.util.List;


public interface  BaseMapper<REQ,RES> {
    RES requestToResponse(REQ req);
    REQ responseToRequest(RES req);
    List<RES> toEntityList(List<REQ> dtos);
    List<REQ> toDTOList(List<RES> entities);
}
