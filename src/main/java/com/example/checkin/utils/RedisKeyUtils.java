package com.example.checkin.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RedisKeyUtils {
    private static final String CHECKIN_PREFIX = "checkin:user:%d:month:%s";
    private static final String CHECKIN_COUNT_SUFFIX = ":count";
    private static final String LOCK_POINTS_PREFIX = "lock:user:%d:points";
    private static final String LOCK_POINTS_SAVE_DATA_PREFIX = "lockTran:user:%d:points";
    private static final String PROFILE_PREFIX = "getProfile:user:%d";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static String getPointHistoryCacheKey(Long userId, int page, int size) {
        return String.format("pointHistory:user:%d:page:%d:size:%d", userId, page, size);
    }
    public static String getCheckInSetKey(Long userId, LocalDate date) {
        String month = date.format(MONTH_FORMATTER);
        return String.format(CHECKIN_PREFIX, userId, month);
    }

    public static String getCheckInCountKey(Long userId, LocalDate date) {
        return getCheckInSetKey(userId, date) + CHECKIN_COUNT_SUFFIX;
    }

    public static String getPointsLockKey(Long userId) {
        return String.format(LOCK_POINTS_PREFIX, userId);
    }

    public static String getPointsLockKeySaveData(Long userId) {
        return String.format(LOCK_POINTS_SAVE_DATA_PREFIX, userId);
    }
    public static String getProfileCacheKey(Long userId) {
        return String.format(PROFILE_PREFIX, userId);
    }
}
