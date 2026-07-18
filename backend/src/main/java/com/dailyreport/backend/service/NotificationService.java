package com.dailyreport.backend.service;

import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * メール通知サービス
 *
 * スケジューラーから呼ばれる通知処理のビジネスロジックをここに集約する。
 * スケジューラー（NotificationScheduler）はタイミングの制御のみを担い、
 * 実際の「誰に何を送るか」はこのサービスが責任を持つ。
 *
 * 【@Transactional(readOnly = true)】
 * このサービスは DB の読み取りのみ行うため readOnly = true を指定する。
 * パフォーマンス最適化（スナップショット不要）に加え、誤った書き込みを防ぐ安全策にもなる。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;
    private final DailyReportRepository reportRepository;
    private final MailService mailService;

    /**
     * 朝の通知メールを対象ユーザーへ送信する。
     *
     * @param targetTime 業務開始時刻（現在時刻 + 30分 を渡すことで30分前通知を実現）
     * @param reportDate メールに載せる日報の日付（通常は昨日の日付）
     */
    public void sendMorningNotifications(LocalTime targetTime, LocalDate reportDate) {
        // 「業務開始時刻 = targetTime かつ 通知ON」のユーザーを絞り込む
        List<User> targetUsers = userRepository.findByWorkStartTimeAndNotificationEnabled(
                targetTime, true
        );

        log.info("対象ユーザー数: {}", targetUsers.size());

        for (User user : targetUsers) {
            // 指定日の日報を取得し、まとめ欄が入力済みの場合のみ送信する
            reportRepository.findByUserIdAndReportDate(user.getId(), reportDate)
                    .ifPresent(report -> {
                        if (report.getSummary() != null && !report.getSummary().isBlank()) {
                            log.info("メール送信: {}", user.getEmail());
                            mailService.sendDailyReportSummary(
                                    user.getEmail(),
                                    user.getName(),
                                    reportDate,
                                    report.getSummary()
                            );
                        }
                    });
        }
    }
}
