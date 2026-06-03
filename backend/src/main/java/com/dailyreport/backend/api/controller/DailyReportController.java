package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.DailyReportRequest;
import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.service.DailyReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日報コントローラー
 *
 * 【役割】日報に関するHTTPリクエストを受け取り、DailyReportService に処理を委譲する。
 *
 * 【@AuthenticationPrincipal UserDetails userDetails】
 * JwtFilter がリクエストヘッダーの JWT を検証し、SecurityContext に保存した認証情報を
 * ここで受け取る仕組み。セッション不要で「誰のリクエストか」を特定できる。
 * userDetails.getUsername() はこのアプリではメールアドレスを返す（JwtFilter の実装による）。
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    /**
     * GET /api/reports（自分の日報一覧取得）
     * JWT から取得したメールアドレスを使って、ログイン中のユーザーの日報だけを返す。
     */
    @GetMapping
    public ResponseEntity<List<DailyReportResponse>> getMyReports(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.getMyReports(userDetails.getUsername()));
    }

    /**
     * GET /api/reports/public（公開日報一覧取得）
     * 全ユーザーの公開日報を返す。@AuthenticationPrincipal 不要（誰の日報か問わない）。
     * SecurityConfig で permitAll にせず認証は必要としている（ログインユーザーのみ閲覧可）。
     */
    @GetMapping("/public")
    public ResponseEntity<List<DailyReportResponse>> getPublicReports() {
        return ResponseEntity.ok(dailyReportService.getPublicReports());
    }

    /**
     * GET /api/reports/{id}（日報詳細取得）
     * 【@PathVariable】URLパス内の {id} をメソッド引数に自動バインドする。
     * Service 内で「自分の日報か」を検証し、他人の日報は 403 を返す。
     */
    @GetMapping("/{id}")
    public ResponseEntity<DailyReportResponse> getMyReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.getMyReport(id, userDetails.getUsername()));
    }

    /**
     * POST /api/reports（日報作成）
     * 作成成功時は 201 Created を返す。
     */
    @PostMapping
    public ResponseEntity<DailyReportResponse> create(
            @RequestBody @Valid DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DailyReportResponse response = dailyReportService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/reports/{id}（日報更新）
     * 全フィールドを上書きする。Service 内で所有者チェックあり。
     */
    @PutMapping("/{id}")
    public ResponseEntity<DailyReportResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.update(id, request, userDetails.getUsername()));
    }

    /**
     * DELETE /api/reports/{id}（日報削除）
     * 削除成功時は 204 No Content を返す（レスポンスボディなし）。
     * Service 内で所有者チェックあり。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        dailyReportService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
