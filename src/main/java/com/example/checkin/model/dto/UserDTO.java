package com.example.checkin.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private Long totalPoints;
}