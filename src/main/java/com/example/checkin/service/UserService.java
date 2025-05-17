package com.example.checkin.service;


import com.example.checkin.model.dto.UserDTO;

public interface UserService {
    public UserDTO createUser(UserDTO userDTO);
    public UserDTO getUserProfile(Long userId);
}
