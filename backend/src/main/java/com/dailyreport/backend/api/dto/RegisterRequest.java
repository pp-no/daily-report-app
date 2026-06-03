package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

/**
 * ユーザー登録リクエストDTO
 *
 * 【@Size(min = 8)】パスワードの最低文字数チェック。
 * ハッシュ化（BCrypt）は Service 層で行うため、ここでは生パスワードを受け取る。
 * HTTPS 通信前提なら平文で受け取っても問題ない（ネットワーク上では暗号化されている）。
 */
public record RegisterRequest(
        @NotBlank(message = "名前は必須です")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, message = "パスワードは8文字以上です")
        String password,

        /** 業務開始時刻。nullの場合はEntity側のデフォルト値（09:00）を使用する */
        LocalTime workStartTime
) {}
