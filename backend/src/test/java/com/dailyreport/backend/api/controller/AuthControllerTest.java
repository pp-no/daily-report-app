package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.config.SecurityConfig;
import com.dailyreport.backend.security.JwtFilter;
import com.dailyreport.backend.security.JwtUtil;
import com.dailyreport.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController の統合テスト。
 *
 * @Import: @WebMvcTest はコントローラー層のみスキャンするため SecurityConfig と JwtFilter が
 *          除外される。明示的にインポートすることで /api/auth/** の permitAll() が適用される。
 * /api/auth/** は SecurityConfig で permitAll() のため、@WithMockUser は不要。
 * バリデーションエラーと正常系のSet-Cookieヘッダーを検証する。
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // JwtFilter の依存をモックに差し替える
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // =========================================================
    // POST /api/auth/register  ユーザー登録
    // =========================================================

    @Test
    void register_正常系_201とCookieが設定される() throws Exception {
        when(authService.register(any())).thenReturn("mock.jwt.token");

        String requestBody = """
                {
                    "name": "テストユーザー",
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Set-Cookie", containsString("token=mock.jwt.token")));
    }

    @Test
    void register_名前が空の場合_400が返る() throws Exception {
        // @NotBlank(message = "名前は必須です") に違反
        String invalidRequestBody = """
                {
                    "name": "",
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_メールアドレスの形式が不正な場合_400が返る() throws Exception {
        // @Email に違反
        String invalidRequestBody = """
                {
                    "name": "テストユーザー",
                    "email": "not-an-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // POST /api/auth/login  ログイン
    // =========================================================

    @Test
    void login_正常系_200とCookieが設定される() throws Exception {
        when(authService.login(any())).thenReturn("mock.jwt.token");

        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("token=mock.jwt.token")));
    }

    @Test
    void login_パスワードが空の場合_400が返る() throws Exception {
        // @NotBlank(message = "パスワードは必須です") に違反
        String invalidRequestBody = """
                {
                    "email": "test@example.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    // =========================================================
    // POST /api/auth/logout  ログアウト
    // =========================================================

    @Test
    void logout_正常系_200とCookieがクリアされる() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("token=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}
