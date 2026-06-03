package com.dailyreport.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:onryki.work@gmail.com}")
    private String fromAddress;

    public void sendDailyReportSummary(String to, String userName, LocalDate reportDate, String summary) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
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
