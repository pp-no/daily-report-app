package com.dailyreport.backend.api.exception;

/**
 * エラーレスポンスDTO
 *
 * 全エラーレスポンスのJSON形式を統一するためのクラス。
 * 例: {"message": "日報が見つかりません: 42"}
 *
 * フロントエンドは常にこの形式でエラーメッセージを受け取れるため、
 * エラーハンドリングのコードをシンプルにできる。
 */
public record ErrorResponse(String message) {}
