package com.example.checkin.controller;

import com.example.checkin.model.dto.CheckInStatusDTO;
import com.example.checkin.model.dto.PointHistoryDTO;
import com.example.checkin.model.dto.UserDTO;
import com.example.checkin.model.request.DeductPointsRequest;
import com.example.checkin.service.CheckInService;
import com.example.checkin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CheckInService checkInService;

    @PostMapping
    public UserDTO createUser( @RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @GetMapping("/{userId}")
    public UserDTO getUserProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @GetMapping("/{userId}/check-in-status")
    public List<CheckInStatusDTO> getCheckInStatus(@PathVariable Long userId) {
        return checkInService.getCheckInStatus(userId);
    }

    @PostMapping("/{userId}/check-in")
    public void checkIn(@PathVariable Long userId) {
        checkInService.checkIn(userId);
    }

    @GetMapping("/{userId}/point-history")
    public Page<PointHistoryDTO> getPointHistory(@PathVariable Long userId, @RequestParam int page, @RequestParam  int size) {
        return checkInService.getPointHistory(userId, page,size);
    }

    @PostMapping("/{userId}/deduct-points")
    public void deductPoints(@PathVariable Long userId,  @RequestBody DeductPointsRequest request) {
        checkInService.deductPoints(userId, request);
    }
}