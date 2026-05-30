package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.DailyReportRequest;
import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.domain.entity.DailyReport;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.exception.ReportAccessDeniedException;
import com.dailyreport.backend.domain.exception.ReportNotFoundException;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyReportService {

    private final DailyReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DailyReportResponse> getMyReports(String email) {
        User user = findUserByEmail(email);
        return reportRepository.findByUserIdOrderByReportDateDesc(user.getId())
                .stream().map(DailyReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DailyReportResponse getMyReport(Long id, String email) {
        DailyReport report = findReportById(id);
        validateOwner(report, email);
        return DailyReportResponse.from(report);
    }

    public DailyReportResponse create(DailyReportRequest request, String email) {
        User user = findUserByEmail(email);
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

    public void delete(Long id, String email) {
        DailyReport report = findReportById(id);
        validateOwner(report, email);
        reportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<DailyReportResponse> getPublicReports() {
        return reportRepository.findByIsPublicTrueOrderByReportDateDesc()
                .stream().map(DailyReportResponse::from).toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: " + email));
    }

    private DailyReport findReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
    }

    private void validateOwner(DailyReport report, String email) {
        if (!report.getUser().getEmail().equals(email)) {
            throw new ReportAccessDeniedException();
        }
    }
}
