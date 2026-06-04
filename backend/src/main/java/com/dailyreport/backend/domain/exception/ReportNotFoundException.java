package com.dailyreport.backend.domain.exception;

/**
 * 日報が見つからない場合にスローする例外
 *
 * 【RuntimeException を継承する理由】
 * Java の検査例外（Exception）と違い、呼び出し元で catch を強制されない。
 * Spring の例外ハンドリング（GlobalExceptionHandler）で一元的に処理するため、
 * 各メソッドに throws 宣言を書かなくて済む。
 *
 * GlobalExceptionHandler がこの例外をキャッチして 404 Not Found を返す。
 */
public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long id) {
        super("日報が見つかりません: id=" + id);
    }
}
