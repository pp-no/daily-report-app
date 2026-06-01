package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

public record UserRequest(
        @NotBlank(message = "名前は必須です")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        LocalTime workStartTime,

        boolean notificationEnabled
) {}
