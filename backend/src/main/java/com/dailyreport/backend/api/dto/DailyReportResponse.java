package com.dailyreport.backend.api.dto;

import com.dailyreport.backend.domain.entity.DailyReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日報レスポンスDTO
 *
 * 【役割】フロントエンドへ返すデータの型。Entity をそのまま返さず DTO に詰め替える理由：
 * - Entity には password 等の機密フィールドや JPA の内部状態が含まれる場合があるため
 * - API の仕様を Entity の構造変更から切り離すため
 *
 * 【static from() メソッド】ファクトリーメソッドパターン。
 * Entity → DTO への変換ロジックをここに集約し、呼び出し元をシンプルに保つ。
 * Service では DailyReportResponse.from(report) と1行で変換できる。
 */
public record DailyReportResponse(
        Long id,
        String userName,
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
                report.getUser().getName(),
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
