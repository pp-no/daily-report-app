package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

/**
 * プロフィール更新リクエストDTO
 *
 * 名前・メールアドレス・業務開始時刻・通知設定を一括で受け取る。
 * workStartTime は null の場合は変更しない（Service 側で制御）。
 */
public record UserRequest(
        @NotBlank(message = "名前は必須です")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        // null の場合は Service 側で既存の値を保持する
        LocalTime workStartTime,

        boolean notificationEnabled
) {}
