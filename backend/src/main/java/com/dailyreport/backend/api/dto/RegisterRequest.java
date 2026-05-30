package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record RegisterRequest(
        @NotBlank(message = "名前は必須です")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, message = "パスワードは8文字以上です")
        String password,

        /** 業務開始時刻。nullの場合はデフォルト値（09:00）を使用する */
        LocalTime workStartTime
) {}
