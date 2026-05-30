package com.dailyreport.backend.api.dto;

import com.dailyreport.backend.domain.entity.DailyReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyReportResponse(
        Long id,
        LocalDate reportDate,
        String title,
        String todayTasks,
        String tomorrowTasks,
        String impression,
        String summary,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DailyReportResponse from(DailyReport report) {
        return new DailyReportResponse(
                report.getId(),
                report.getReportDate(),
                report.getTitle(),
                report.getTodayTasks(),
                report.getTomorrowTasks(),
                report.getImpression(),
                report.getSummary(),
                report.isPublic(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
