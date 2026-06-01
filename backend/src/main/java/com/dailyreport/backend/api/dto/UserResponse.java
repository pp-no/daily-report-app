package com.dailyreport.backend.api.dto;

import com.dailyreport.backend.domain.entity.User;

import java.time.LocalTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalTime workStartTime,
        boolean notificationEnabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getWorkStartTime(),
                user.isNotificationEnabled()
        );
    }
}
