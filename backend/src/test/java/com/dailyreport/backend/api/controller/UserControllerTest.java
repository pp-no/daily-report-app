package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.UserResponse;
import com.dailyreport.backend.config.SecurityConfig;
import com.dailyreport.backend.security.JwtFilter;
import com.dailyreport.backend.security.JwtUtil;
import com.dailyreport.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // =========================================================
    // GET /api/users/me
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void getProfile_認証済みユーザー_200とプロフィールが返る() throws Exception {
        UserResponse response = new UserResponse(1L, "テストユーザー", "test@example.com",
                LocalTime.of(9, 0), true);
        when(userService.getProfile("test@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("テストユーザー"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.notificationEnabled").value(true));
    }

    @Test
    void getProfile_未認証の場合_401が返る() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // PUT /api/users/me
    // =========================================================

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_正常系_200と更新後プロフィールが返る() throws Exception {
        UserResponse response = new UserResponse(1L, "更新後名前", "test@example.com",
                LocalTime.of(10, 0), false);
        when(userService.updateProfile(any(), eq("test@example.com"))).thenReturn(response);

        String requestBody = """
                {
                    "name": "更新後名前",
                    "email": "test@example.com",
                    "workStartTime": "10:00:00",
                    "notificationEnabled": false
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新後名前"))
                .andExpect(jsonPath("$.notificationEnabled").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_名前が空の場合_400が返る() throws Exception {
        String invalidRequestBody = """
                {
                    "name": "",
                    "email": "test@example.com",
                    "notificationEnabled": true
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(), any());
    }
}
