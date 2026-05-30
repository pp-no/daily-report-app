package com.dailyreport.backend.domain.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long id) {
        super("日報が見つかりません: id=" + id);
    }
}
