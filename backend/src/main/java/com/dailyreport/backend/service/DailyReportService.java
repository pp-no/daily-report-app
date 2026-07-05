package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.DailyReportRequest;
import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.domain.entity.DailyReport;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.exception.DuplicateReportException;
import com.dailyreport.backend.domain.exception.ReportAccessDeniedException;
import com.dailyreport.backend.domain.exception.ReportNotFoundException;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 日報サービス
 *
 * 【@Transactional】クラス全体に適用。メソッド内で例外が発生すると DB の変更がロールバックされる。
 * 例えば create() の途中で例外が発生しても、中途半端なデータが残らない。
 *
 * 読み取り専用のメソッドには @Transactional(readOnly = true) を付ける。
 * これにより不要なロックを避けてパフォーマンスを向上させられる。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DailyReportService {

    private final DailyReportRepository reportRepository;
    private final UserRepository userRepository;

    /** 自分の日報一覧を日付の新しい順で返す。 */
    @Transactional(readOnly = true)
    public List<DailyReportResponse> getMyReports(String email) {
        User user = findUserByEmail(email);
        return reportRepository.findByUserIdOrderByReportDateDesc(user.getId())
                .stream().map(DailyReportResponse::from).toList();
    }

    /** 日報の詳細を返す。他人の日報にアクセスしようとすると 403 を返す。 */
    @Transactional(readOnly = true)
    public DailyReportResponse getMyReport(Long id, String email) {
        DailyReport report = findReportById(id);
        validateOwner(report, email);
        return DailyReportResponse.from(report);
    }

    /** 日報を新規作成してDBに保存する。同じ日付の日報が既に存在する場合は例外をスロー。 */
    public DailyReportResponse create(DailyReportRequest request, String email) {
        User user = findUserByEmail(email);
        if (reportRepository.findByUserIdAndReportDate(user.getId(), request.reportDate()).isPresent()) {
            throw new DuplicateReportException(request.reportDate());
        }
        DailyReport report = new DailyReport();
        report.setUser(user);
        report.setReportDate(request.reportDate());
        report.setTitle(request.title());
        report.setTodayTasks(request.todayTasks());
        report.setTomorrowTasks(request.tomorrowTasks());
        report.setImpression(request.impression());
        report.setSummary(request.summary());
        report.setPublic(request.isPublic());
        return DailyReportResponse.from(reportRepository.save(report));
    }

    /**
     * 日報を更新する。@Transactional が有効なため、
     * ここでは save() を呼ばなくても変更が自動でDBに反映される（ダーティチェック）。
     */
    public DailyReportResponse update(Long id, DailyReportRequest request, String email) {
        DailyReport report = findReportById(id);
        validateOwner(report, email);
        report.setReportDate(request.reportDate());
        report.setTitle(request.title());
        report.setTodayTasks(request.todayTasks());
        report.setTomorrowTasks(request.tomorrowTasks());
        report.setImpression(request.impression());
        report.setSummary(request.summary());
        report.setPublic(request.isPublic());
        return DailyReportResponse.from(report);
    }

    /** 日報を削除する。所有者でなければ 403 を返す。 */
    public void delete(Long id, String email) {
        DailyReport report = findReportById(id);
        validateOwner(report, email);
        reportRepository.delete(report);
    }

    /** 全ユーザーの公開日報を返す。誰の日報かは問わない。 */
    @Transactional(readOnly = true)
    public List<DailyReportResponse> getPublicReports() {
        return reportRepository.findByIsPublicTrueOrderByReportDateDesc()
                .stream().map(DailyReportResponse::from).toList();
    }

    // ---- private ヘルパーメソッド ----

    /** メールアドレスでユーザーを取得する。見つからない場合は例外をスロー（通常は起きない）。 */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: " + email));
    }

    /** IDで日報を取得する。見つからない場合は 404 を返すための例外をスロー。 */
    private DailyReport findReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
    }

    /**
     * 日報の所有者チェック。
     * 他人の日報を操作しようとした場合は 403 を返すための例外をスロー。
     * Controller 側では URL の id だけを受け取るため、ここでの検証が不可欠。
     */
    private void validateOwner(DailyReport report, String email) {
        if (!report.getUser().getEmail().equals(email)) {
            throw new ReportAccessDeniedException();
        }
    }
}
