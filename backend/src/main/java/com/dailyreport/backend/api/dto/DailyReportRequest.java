package com.dailyreport.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DailyReportRequest(
        @NotBlank(message = "タイトルは必須です")
        @Size(max = 200, message = "タイトルは200文字以内です")
        String title,

        @NotBlank(message = "今日やったことは必須です")
        String todayTasks,

        @NotBlank(message = "明日やることは必須です")
        String tomorrowTasks,

        String impression,

        String summary,

        @NotNull(message = "日付は必須です")
        LocalDate reportDate,

        boolean isPublic
) {}
