package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.AuthResponse;
import com.dailyreport.backend.api.dto.LoginRequest;
import com.dailyreport.backend.api.dto.RegisterRequest;
import com.dailyreport.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証コントローラー
 *
 * 【役割】HTTPリクエストを受け取り、Service へつなぐだけの入出力窓口。
 * ビジネスロジック（入力検証以外）は持たない。
 *
 * 【@RestController】
 * @Controller + @ResponseBody の組み合わせ。
 * 戻り値のオブジェクトを自動でJSONに変換してレスポンスに書き込む。
 *
 * 【@RequestMapping("/api/auth")】
 * このクラス全体のURLプレフィックス。
 * 各メソッドのパスと組み合わさって最終的なURLになる（例: /api/auth/login）。
 *
 * 【@RequiredArgsConstructor】
 * final フィールドをすべて引数に取るコンストラクタを Lombok が自動生成。
 * Spring はそのコンストラクタを使ってDI（依存性の注入）を行う。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register（ユーザー登録）
     *
     * 【@RequestBody】リクエストボディのJSONをJavaオブジェクトに自動変換する。
     * 【@Valid】RegisterRequest に定義されたバリデーションアノテーション（@NotBlank等）を実行する。
     *   バリデーション失敗時は GlobalExceptionHandler が 400 Bad Request を返す。
     *
     * 【201 Created】リソース作成成功時のHTTPステータス。
     *   200 ではなく 201 を返すのが REST の慣習。
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login（ログイン）
     *
     * 認証成功時は 200 OK と JWT トークンを返す。
     * 認証失敗時は Spring Security が例外を投げて 401 を返すため、ここには到達しない。
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
