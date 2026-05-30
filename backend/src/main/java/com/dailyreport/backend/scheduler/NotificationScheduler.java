package com.dailyreport.backend.scheduler;

import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import com.dailyreport.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final DailyReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    // 毎分チェックして、業務開始30分前のユーザーに通知
    @Scheduled(cron = "0 * * * * *")
    public void sendMorningNotification() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("スケジューラー実行: now={}, target={}", now, now.plusMinutes(30));

        List<User> targetUsers = userRepository.findByWorkStartTimeAndNotificationEnabled(
                now.plusMinutes(30), true
        );

        log.info("対象ユーザー数: {}", targetUsers.size());

        for (User user : targetUsers) {
            reportRepository.findByUserIdAndReportDate(user.getId(), yesterday)
                    .ifPresent(report -> {
                        if (report.getSummary() != null && !report.getSummary().isBlank()) {
                            log.info("メール送信: {}", user.getEmail());
                            mailService.sendDailyReportSummary(
                                    user.getEmail(),
                                    user.getName(),
                                    yesterday,
                                    report.getSummary()
                            );
                        }
                    });
        }
    }
}
