package com.dailyreport.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * メール送信サービス
 *
 * 【JavaMailSender】Spring が提供するメール送信インターフェース。
 * application.properties の spring.mail.* 設定を元に Amazon SES の SMTP に接続する。
 *
 * 【FROM = TO の理由】
 * このアプリは「自分に自分の日報を送る」仕組みのため、送信元と宛先は同じユーザーのメールアドレス。
 * Amazon SES のサンドボックス環境では送信元も検証済みアドレスである必要があるため、
 * 固定のFROMアドレスを使う代わりにユーザー自身のアドレスをFROMに設定している。
 */
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * 日報サマリーをメールで送信する。
     *
     * @param to         送信先メールアドレス（FROMも同じアドレスを使う）
     * @param userName   メール本文中に表示するユーザー名
     * @param reportDate 日報の日付（件名・本文に表示）
     * @param summary    日報のまとめ欄の内容
     */
    public void sendDailyReportSummary(String to, String userName, LocalDate reportDate, String summary) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(to);
        message.setTo(to);
        message.setSubject("【日報サマリー】昨日の振り返り - " + reportDate);
        message.setText(
                userName + "さん、おはようございます。\n" +
                "昨日（" + reportDate + "）のまとめです。\n\n" +
                "━━━━━━━━━━━━━━━━━━\n" +
                summary + "\n" +
                "━━━━━━━━━━━━━━━━━━\n\n" +
                "本日もよろしくお願いします。"
        );
        mailSender.send(message);
    }
}
