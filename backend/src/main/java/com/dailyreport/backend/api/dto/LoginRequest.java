package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストDTO
 *
 * 【@Email】メールアドレスの形式チェック（@ が含まれているか等）。
 * DB に存在するかどうかは Service 層で検証する（形式チェックとは別の関心事）。
 */
public record LoginRequest(
        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        @NotBlank(message = "パスワードは必須です")
        String password
) {}
