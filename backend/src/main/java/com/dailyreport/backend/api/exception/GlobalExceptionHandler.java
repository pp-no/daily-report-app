package com.dailyreport.backend.api.exception;

import com.dailyreport.backend.domain.exception.ReportAccessDeniedException;
import com.dailyreport.backend.domain.exception.ReportNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * グローバル例外ハンドラー
 *
 * 【@RestControllerAdvice】
 * 全コントローラーで発生した例外をここで一元的にキャッチして、
 * 適切なHTTPレスポンスに変換する仕組み。
 * 各コントローラーに try-catch を書かなくて済む。
 *
 * 【フロー】
 * Service が例外をスロー → コントローラーをスルー → ここでキャッチ → HTTPレスポンス生成
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日報が存在しない場合 → 404 Not Found
     * Service で reportRepository.findById() が空の場合にスローされる。
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReportNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * 他人の日報にアクセスしようとした場合 → 403 Forbidden
     * Service で validateOwner() がスローする。
     */
    @ExceptionHandler(ReportAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(ReportAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @Valid によるバリデーション失敗 → 400 Bad Request
     * 複数フィールドでエラーがある場合は ", " で連結してまとめて返す。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /**
     * メールアドレス重複など業務上の不正操作 → 400 Bad Request
     * Service で throw new IllegalArgumentException(...) した場合にキャッチされる。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }
}
