package com.dailyreport.backend.domain.exception;

/**
 * 他人の日報にアクセスしようとした場合にスローする例外
 *
 * DailyReportService の validateOwner() メソッドでスローされる。
 * GlobalExceptionHandler がこの例外をキャッチして 403 Forbidden を返す。
 *
 * 404 ではなく 403 を返すことで「日報は存在するが権限がない」ことを明示する。
 * ただし、存在しないIDか権限がないIDかをクライアントに区別させたくない場合は
 * 404 に統一する設計もある（情報漏洩対策）。
 */
public class ReportAccessDeniedException extends RuntimeException {
    public ReportAccessDeniedException() {
        super("この日報へのアクセス権限がありません");
    }
}
