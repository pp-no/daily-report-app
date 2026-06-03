package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.UserRequest;
import com.dailyreport.backend.api.dto.UserResponse;
import com.dailyreport.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ユーザーコントローラー
 *
 * 【役割】プロフィール取得・更新のエンドポイントを提供する。
 * /me というパスは「ログイン中の自分」を指す REST の慣習的な表現。
 * URL に ID を含めないことで、他人のプロフィールを操作できない構造になっている。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me（自分のプロフィール取得）
     * JWT から取得したメールアドレスを使って自分の情報を返す。
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    /**
     * PUT /api/users/me（プロフィール更新）
     * 名前・メールアドレス・業務開始時刻・通知設定を一括更新する。
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody @Valid UserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateProfile(request, userDetails.getUsername()));
    }
}
