package com.dailyreport.backend.domain.repository;

import com.dailyreport.backend.domain.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);

    List<DailyReport> findByUserIdOrderByReportDateDesc(Long userId);

    List<DailyReport> findByIsPublicTrueOrderByReportDateDesc();

    @Query("SELECT r FROM DailyReport r WHERE r.reportDate = :date AND r.user.notificationEnabled = true")
    List<DailyReport> findReportsForNotification(@Param("date") LocalDate date);
}
