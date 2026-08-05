package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.config.SecurityConfig;
import com.dailyreport.backend.domain.exception.ReportNotFoundException;
import com.dailyreport.backend.security.JwtFilter;
import com.dailyreport.backend.security.JwtUtil;
import com.dailyreport.backend.service.DailyReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DailyReportController の統合テスト。
 *
 * @WebMvcTest: Spring MVC レイヤーのみ起動（DB・メール送信は起動しない）
 * @Import: @WebMvcTest はコントローラー層のみスキャンするため SecurityConfig と JwtFilter が
 *          除外される。明示的にインポートすることで自作のセキュリティルール（CSRF無効・permitAll等）が適用される。
 * @MockitoBean: Service・JwtUtil・UserDetailsService をモックに差し替える
 * @WithMockUser: Spring Security Test が SecurityContext にモックユーザーをセットする
 *               → JwtFilter はヘッダーなしで素通りし、モックユーザーで認証済みとなる
 */
@WebMvcTest(DailyReportController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class DailyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyReportService dailyReportService;

    // JwtFilter が依存するビーンをモックに差し替える
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // =========================================================
    // GET /api/reports  自分の日報一覧
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyReports_認証済みユーザー_200と日報一覧が返る() throws Exception {
        DailyReportResponse response = new DailyReportResponse(
                1L, "test@example.com", LocalDate.of(2026, 5, 30), "テスト日報",
                "今日やったこと", "明日やること", "所感", "まとめ",
                false, null, null
        );
        when(dailyReportService.getMyReports("test@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("テスト日報"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getMyReports_未認証の場合_401が返る() throws Exception {
        // @WithMockUser なし → SecurityContext にユーザーなし → 401
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // GET /api/reports/public  公開日報一覧（認証不要）
    // =========================================================

    @Test
    void getPublicReports_認証なしでも_200と公開日報が返る() throws Exception {
        DailyReportResponse response = new DailyReportResponse(
                2L, "other@example.com", LocalDate.of(2026, 5, 29), "公開日報",
                "公開タスク", "公開予定", null, null,
                true, null, null
        );
        when(dailyReportService.getPublicReports()).thenReturn(List.of(response));

        // SecurityConfig で /api/reports/public は permitAll() なので認証なしで OK
        mockMvc.perform(get("/api/reports/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("公開日報"));
    }

    // =========================================================
    // POST /api/reports  日報作成
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void createReport_正常系_201と作成された日報が返る() throws Exception {
        DailyReportResponse response = new DailyReportResponse(
                1L, "test@example.com", LocalDate.of(2026, 5, 30), "新規日報",
                "今日やったこと", "明日やること", null, null,
                false, null, null
        );
        when(dailyReportService.create(any(), eq("test@example.com"))).thenReturn(response);

        String requestBody = """
                {
                    "title": "新規日報",
                    "todayTasks": "今日やったこと",
                    "tomorrowTasks": "明日やること",
                    "reportDate": "2026-05-30",
                    "isPublic": false
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("新規日報"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createReport_必須項目が空の場合_400が返る() throws Exception {
        // title・todayTasks・tomorrowTasks・reportDate がすべて欠けているリクエスト
        String invalidRequestBody = """
                {
                    "isPublic": false
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        // バリデーションエラーのためサービスは呼ばれない
        verify(dailyReportService, never()).create(any(), any());
    }

    // =========================================================
    // DELETE /api/reports/{id}  日報削除
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteReport_正常系_204が返る() throws Exception {
        doNothing().when(dailyReportService).delete(1L, "test@example.com");

        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isNoContent());

        verify(dailyReportService, times(1)).delete(1L, "test@example.com");
    }

    // =========================================================
    // GET /api/reports/{id}  単件取得（例外ハンドリング確認）
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyReport_日報が存在しない場合_404が返る() throws Exception {
        when(dailyReportService.getMyReport(99L, "test@example.com"))
                .thenThrow(new ReportNotFoundException(99L));

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
