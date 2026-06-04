package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import com.dailyreport.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 通知コントローラー
 *
 * 【役割】プロフィール画面の「今すぐ送信」ボタンに対応するエンドポイントを提供する。
 * スケジューラーは毎日自動で動くが、このエンドポイントは任意のタイミングで
 * 手動でメール送信を確認したいときに使う。
 *
 * 【Service を経由しない理由】
 * 通知専用の Service を作るほどビジネスロジックが複雑でないため、
 * Controller から直接 Repository と MailService を呼んでいる。
 * 処理が増えるようなら NotificationService に切り出すのが望ましい。
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserRepository userRepository;
    private final DailyReportRepository reportRepository;
    private final MailService mailService;

    /**
     * POST /api/notifications/send（即時メール送信）
     *
     * ログイン中のユーザーに対して昨日の日報サマリーを今すぐ送信する。
     * 昨日の日報が存在しない、またはサマリーが未入力の場合はダミーテキストで送信する。
     * これにより日報の有無に関わらず動作確認ができる。
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Tokyo")).minusDays(1);

        // 昨日の日報を取得し、サマリーが入力済みであればその内容を使う
        // 日報がない・サマリーが空の場合はダミーテキストにフォールバックする
        String summary = reportRepository.findByUserIdAndReportDate(user.getId(), yesterday)
                .map(report -> report.getSummary() != null && !report.getSummary().isBlank()
                        ? report.getSummary()
                        : "（サマリーが未入力のため、テスト用のダミーテキストを使用しています）")
                .orElse("（昨日の日報が存在しないため、テスト用のダミーテキストを使用しています）");

        mailService.sendDailyReportSummary(user.getEmail(), user.getName(), yesterday, summary);

        return ResponseEntity.ok("テストメールを送信しました: " + user.getEmail());
    }
}
