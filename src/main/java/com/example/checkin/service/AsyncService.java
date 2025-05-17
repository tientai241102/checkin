package com.example.checkin.service;

import java.time.LocalDate;

public interface AsyncService {
     void saveCheckInAsync(Long userId, LocalDate checkInDate, int points, int checkInCountToday, String redisSetKey, String redisCountKey, String today);
}
