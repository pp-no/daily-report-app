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

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserRepository userRepository;
    private final DailyReportRepository reportRepository;
    private final MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Tokyo")).minusDays(1);

        String summary = reportRepository.findByUserIdAndReportDate(user.getId(), yesterday)
                .map(report -> report.getSummary() != null && !report.getSummary().isBlank()
                        ? report.getSummary()
                        : "（サマリーが未入力のため、テスト用のダミーテキストを使用しています）")
                .orElse("（昨日の日報が存在しないため、テスト用のダミーテキストを使用しています）");

        mailService.sendDailyReportSummary(user.getEmail(), user.getName(), yesterday, summary);

        return ResponseEntity.ok("テストメールを送信しました: " + user.getEmail());
    }
}
