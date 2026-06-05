package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * パスワード変更リクエスト DTO
 *
 * currentPassword で現在のパスワードを照合してから newPassword に変更する。
 * confirmPassword はフロント側の入力ミス防止用で、サービス層で newPassword と一致するか検証する。
 */
public record ChangePasswordRequest(
        @NotBlank(message = "現在のパスワードを入力してください")
        String currentPassword,

        @NotBlank(message = "新しいパスワードを入力してください")
        @Size(min = 8, message = "パスワードは8文字以上で入力してください")
        String newPassword,

        @NotBlank(message = "確認用パスワードを入力してください")
        String confirmPassword
) {}
