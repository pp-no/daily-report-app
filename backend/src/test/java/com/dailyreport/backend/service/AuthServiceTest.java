package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.AuthResponse;
import com.dailyreport.backend.api.dto.LoginRequest;
import com.dailyreport.backend.api.dto.RegisterRequest;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.UserRepository;
import com.dailyreport.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService の単体テスト。
 * ユーザー登録・ログインのビジネスロジックを検証する。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // =========================================================
    // register
    // =========================================================

    @Test
    void register_正常系_JWTトークンが返る() {
        RegisterRequest request = new RegisterRequest(
                "テストユーザー",
                "test@example.com",
                "password123",
                LocalTime.of(9, 0)
        );
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mock.jwt.token");

        AuthResponse result = authService.register(request);

        assertThat(result.token()).isEqualTo("mock.jwt.token");
        // パスワードがエンコードされてから保存されることを確認
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_メールアドレスが重複している場合_例外がスロー() {
        RegisterRequest request = new RegisterRequest(
                "テストユーザー",
                "test@example.com",
                "password123",
                null
        );
        // 既存ユーザーが見つかる状態をモック
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("既に使用されています");
        // 重複時は保存が行われないことを確認
        verify(userRepository, never()).save(any());
    }

    // =========================================================
    // login
    // =========================================================

    @Test
    void login_正常系_JWTトークンが返る() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mock.jwt.token");

        AuthResponse result = authService.login(request);

        assertThat(result.token()).isEqualTo("mock.jwt.token");
        // AuthenticationManager.authenticate() が呼ばれたことを確認
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_パスワードが誤っている場合_例外がスロー() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong_password");
        // 認証失敗をモック
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("認証失敗"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
