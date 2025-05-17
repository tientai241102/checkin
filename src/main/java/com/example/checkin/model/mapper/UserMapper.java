package com.example.checkin.model.mapper;

import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserDTO, User> {
}
