package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 日報作成・更新リクエストDTO
 *
 * 【役割】フロントエンドから送られてくるJSONを受け取る型。
 * Controllerで @Valid と組み合わせることで、各フィールドのバリデーションが自動実行される。
 *
 * 【@NotBlank】null・空文字・空白文字のみを禁止する。
 * 【@NotNull】null のみ禁止（空文字は許可）。
 * 【@Size】文字数制限。
 *
 * impression（所感）と summary（まとめ）はバリデーションなし → 任意入力項目。
 */
public record DailyReportRequest(
        @NotBlank(message = "タイトルは必須です")
        @Size(max = 200, message = "タイトルは200文字以内です")
        String title,

        @NotBlank(message = "今日やったことは必須です")
        String todayTasks,

        @NotBlank(message = "明日やることは必須です")
        String tomorrowTasks,

        // 任意入力
        String impression,

        // 任意入力（メール通知で使用する）
        String summary,

        @NotNull(message = "日付は必須です")
        LocalDate reportDate,

        // false がデフォルト（非公開）。true にすると公開日報一覧に表示される。
        boolean isPublic
) {}
