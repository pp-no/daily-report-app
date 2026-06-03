package com.dailyreport.backend.api.dto;

import com.dailyreport.backend.domain.entity.User;

import java.time.LocalTime;

/**
 * ユーザープロフィールレスポンスDTO
 *
 * 【重要】User Entity にはハッシュ化済みパスワードが含まれるが、
 * このDTOには含めていない。Entity をそのまま返すと意図せず機密情報が漏洩するため、
 * 必ずDTOに詰め替えてからレスポンスとして返す。
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        LocalTime workStartTime,
        boolean notificationEnabled
) {
    /** Entity → DTO へのファクトリーメソッド。パスワードは含めない。 */
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
