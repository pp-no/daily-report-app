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
import java.time.ZoneId;
import java.util.List;

/**
 * メール通知スケジューラー
 *
 * 【@Slf4j】ログ出力用のフィールド（log）をLombokが自動生成する。
 * log.info(...) でログを出力できる。
 *
 * 【設計の考え方】
 * 特定の時刻にメールを送るのではなく、毎分「今から30分後が業務開始のユーザー」を検索する。
 * こうすることでユーザーごとに異なる業務開始時刻に対応できる。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final DailyReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    /**
     * 【@Scheduled(cron = "0 * * * * *")】cron式で実行タイミングを指定する。
     *   書式: 秒 分 時 日 月 曜日
     *   "0 * * * * *" = 毎分0秒に実行（= 毎分実行）
     *
     * 【ZoneId.of("Asia/Tokyo")】サーバーのタイムゾーンに依存せず JST で時刻を扱う。
     * ECS（AWS）など本番環境はUTC動作のことが多いため明示的に指定が必要。
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendMorningNotification() {
        ZoneId jst = ZoneId.of("Asia/Tokyo");
        // 秒・ナノ秒を切り捨てて分単位に揃える（DB に保存されている時刻と比較するため）
        LocalTime now = LocalTime.now(jst).withSecond(0).withNano(0);
        LocalDate yesterday = LocalDate.now(jst).minusDays(1);

        log.info("スケジューラー実行: now={}, target={}", now, now.plusMinutes(30));

        // 「現在時刻 + 30分 = 業務開始時刻」かつ「通知ON」のユーザーを絞り込む
        List<User> targetUsers = userRepository.findByWorkStartTimeAndNotificationEnabled(
                now.plusMinutes(30), true
        );

        log.info("対象ユーザー数: {}", targetUsers.size());

        for (User user : targetUsers) {
            // 昨日の日報を取得し、まとめ欄が入力済みの場合のみ送信する
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
