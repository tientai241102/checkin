package com.example.checkin.model.constant;

public enum ErrorCode {
    USER_NOT_FOUND("Người dùng không tồn tại"),
    USERNAME_EXISTS("Tên người dùng đã tồn tại"),
    INVALID_CHECKIN_TIME("Chỉ được điểm danh từ 9-11h hoặc 19-21h"),
    ALREADY_CHECKED_IN("Đã điểm danh hôm nay"),
    MAX_CHECKINS_REACHED("Đã đạt tối đa 7 lần điểm danh trong tháng"),
    INSUFFICIENT_POINTS("Không đủ điểm"),
    LOCK_ACQUISITION_FAILED("Không thể khóa giao dịch"),
    CHECKIN_INTERRUPTED("Giao dịch điểm danh bị gián đoạn"),
    INVALID_CHECKIN_COUNT("Số lần điểm danh không hợp lệ");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
