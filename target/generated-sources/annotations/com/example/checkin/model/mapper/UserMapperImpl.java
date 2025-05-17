package com.example.checkin.model.mapper;

import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.entity.User;
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
public class UserMapperImpl implements UserMapper {

    @Override
    public User requestToResponse(UserDTO req) {
        if ( req == null ) {
            return null;
        }

        User user = new User();

        user.setId( req.getId() );
        user.setUsername( req.getUsername() );
        user.setTotalPoints( req.getTotalPoints() );

        return user;
    }

    @Override
    public UserDTO responseToRequest(User req) {
        if ( req == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setId( req.getId() );
        userDTO.setUsername( req.getUsername() );
        userDTO.setTotalPoints( req.getTotalPoints() );

        return userDTO;
    }

    @Override
    public List<User> toEntityList(List<UserDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<User> list = new ArrayList<User>( dtos.size() );
        for ( UserDTO userDTO : dtos ) {
            list.add( requestToResponse( userDTO ) );
        }

        return list;
    }

    @Override
    public List<UserDTO> toDTOList(List<User> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserDTO> list = new ArrayList<UserDTO>( entities.size() );
        for ( User user : entities ) {
            list.add( responseToRequest( user ) );
        }

        return list;
    }
}
