package com.dailyreport.backend.domain.exception;

import java.time.LocalDate;

/**
 * 同じ日付の日報が既に存在する場合にスローする例外
 *
 * GlobalExceptionHandler がこの例外をキャッチして 409 Conflict を返す。
 */
public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(LocalDate date) {
        super("この日付の日報はすでに存在します: " + date);
    }
}
