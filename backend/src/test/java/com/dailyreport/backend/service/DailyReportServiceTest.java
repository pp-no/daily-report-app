package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.DailyReportRequest;
import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.domain.entity.DailyReport;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.exception.ReportAccessDeniedException;
import com.dailyreport.backend.domain.exception.ReportNotFoundException;
import com.dailyreport.backend.domain.repository.DailyReportRepository;
import com.dailyreport.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DailyReportService の単体テスト。
 * DB・Spring コンテキストは起動せず、Mockito でリポジトリを差し替える。
 *
 * テスト観点:
 *   - 正常系: 期待どおりの戻り値が返るか
 *   - 異常系: 存在しないリソースへのアクセスや権限違反で例外が投げられるか
 */
@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    // ---- モック対象 ----
    @Mock
    private DailyReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    // モックを注入するテスト対象
    @InjectMocks
    private DailyReportService dailyReportService;

    // ---- テスト用の共通データ ----
    private User testUser;
    private DailyReport testReport;
    private DailyReportRequest testRequest;

    @BeforeEach
    void setUp() {
        // テスト用ユーザー
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("テストユーザー");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");

        // テスト用日報
        testReport = new DailyReport();
        testReport.setId(1L);
        testReport.setUser(testUser);
        testReport.setReportDate(LocalDate.of(2026, 5, 30));
        testReport.setTitle("テスト日報");
        testReport.setTodayTasks("今日やったこと");
        testReport.setTomorrowTasks("明日やること");
        testReport.setImpression("所感");
        testReport.setSummary("まとめ");
        testReport.setPublic(false);

        // テスト用リクエスト
        testRequest = new DailyReportRequest(
                "テスト日報",
                "今日やったこと",
                "明日やること",
                "所感",
                "まとめ",
                LocalDate.of(2026, 5, 30),
                false
        );
    }

    // =========================================================
    // getMyReports
    // =========================================================

    @Test
    void getMyReports_正常系_日報一覧が返る() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(reportRepository.findByUserIdOrderByReportDateDesc(1L)).thenReturn(List.of(testReport));

        List<DailyReportResponse> result = dailyReportService.getMyReports("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("テスト日報");
        // リポジトリが1回だけ呼ばれたことを確認
        verify(reportRepository, times(1)).findByUserIdOrderByReportDateDesc(1L);
    }

    @Test
    void getMyReports_ユーザーが存在しない場合_例外がスロー() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyReportService.getMyReports("notfound@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザーが見つかりません");
    }

    // =========================================================
    // getMyReport（単件取得）
    // =========================================================

    @Test
    void getMyReport_正常系_日報が返る() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        DailyReportResponse result = dailyReportService.getMyReport(1L, "test@example.com");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("テスト日報");
    }

    @Test
    void getMyReport_日報が存在しない場合_例外がスロー() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyReportService.getMyReport(99L, "test@example.com"))
                .isInstanceOf(ReportNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getMyReport_他人の日報にアクセスした場合_例外がスロー() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        // testReport の所有者は "test@example.com"、別ユーザーでアクセス
        assertThatThrownBy(() -> dailyReportService.getMyReport(1L, "other@example.com"))
                .isInstanceOf(ReportAccessDeniedException.class);
    }

    // =========================================================
    // create
    // =========================================================

    @Test
    void create_正常系_日報が作成されリポジトリに保存される() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        // save() が呼ばれたら id を付与して返す
        when(reportRepository.save(any(DailyReport.class))).thenAnswer(invocation -> {
            DailyReport saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        DailyReportResponse result = dailyReportService.create(testRequest, "test@example.com");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("テスト日報");
        verify(reportRepository, times(1)).save(any(DailyReport.class));
    }

    // =========================================================
    // update
    // =========================================================

    @Test
    void update_正常系_日報の内容が更新される() {
        DailyReportRequest updateRequest = new DailyReportRequest(
                "更新後タイトル",
                "更新後の今日やったこと",
                "更新後の明日やること",
                null,
                null,
                LocalDate.of(2026, 5, 30),
                true
        );
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        DailyReportResponse result = dailyReportService.update(1L, updateRequest, "test@example.com");

        assertThat(result.title()).isEqualTo("更新後タイトル");
        assertThat(result.isPublic()).isTrue();
    }

    @Test
    void update_他人の日報を更新しようとした場合_例外がスロー() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        assertThatThrownBy(() -> dailyReportService.update(1L, testRequest, "other@example.com"))
                .isInstanceOf(ReportAccessDeniedException.class);
        // save が呼ばれないことを確認
        verify(reportRepository, never()).save(any());
    }

    // =========================================================
    // delete
    // =========================================================

    @Test
    void delete_正常系_日報が削除される() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        dailyReportService.delete(1L, "test@example.com");

        verify(reportRepository, times(1)).delete(testReport);
    }

    @Test
    void delete_他人の日報を削除しようとした場合_例外がスロー() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        assertThatThrownBy(() -> dailyReportService.delete(1L, "other@example.com"))
                .isInstanceOf(ReportAccessDeniedException.class);
        // delete が呼ばれないことを確認
        verify(reportRepository, never()).delete(any());
    }
}
