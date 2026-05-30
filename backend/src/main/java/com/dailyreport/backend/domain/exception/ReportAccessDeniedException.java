package com.dailyreport.backend.domain.exception;

public class ReportAccessDeniedException extends RuntimeException {
    public ReportAccessDeniedException() {
        super("この日報へのアクセス権限がありません");
    }
}
