package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.LoginRequest;
import com.dailyreport.backend.api.dto.RegisterRequest;
import com.dailyreport.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証コントローラー
 *
 * 【HttpOnly Cookie 認証】
 * JWTトークンをレスポンスボディではなく HttpOnly Cookie に格納する。
 * HttpOnly Cookie は JavaScript から読み取れないため XSS 攻撃でトークンを盗まれない。
 * SameSite=Strict により CSRF 攻撃も防御する。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/register（ユーザー登録） */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request, HttpServletResponse response) {
        String token = authService.register(request);
        setTokenCookie(response, token);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** POST /api/auth/login（ログイン） */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        String token = authService.login(request);
        setTokenCookie(response, token);
        return ResponseEntity.ok().build();
    }

    /** POST /api/auth/logout（ログアウト）：Cookieを削除する */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(7 * 24 * 60 * 60) // 7日間
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
