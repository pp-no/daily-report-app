package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.AuthResponse;
import com.dailyreport.backend.api.dto.LoginRequest;
import com.dailyreport.backend.api.dto.RegisterRequest;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.UserRepository;
import com.dailyreport.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認証サービス
 *
 * 【役割】ユーザー登録とログインのビジネスロジックを担当する。
 * Controller は HTTP の入出力のみを担当し、実際の処理はここに集約する。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * ユーザー登録処理。
     * メールアドレスの重複チェック → パスワードハッシュ化 → DB保存 → JWT発行 の順に実行する。
     */
    public AuthResponse register(RegisterRequest request) {
        // 同じメールアドレスが既に存在する場合は登録拒否
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("このメールアドレスは既に使用されています");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        // 生パスワードをBCryptでハッシュ化してからDBに保存する（平文保存は厳禁）
        user.setPassword(passwordEncoder.encode(request.password()));
        // workStartTimeが指定された場合は設定する（nullの場合はEntityのデフォルト値09:00を使用）
        if (request.workStartTime() != null) {
            user.setWorkStartTime(request.workStartTime());
        }
        userRepository.save(user);
        // 登録後すぐにログイン状態にするためJWTを発行して返す
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()));
    }

    /**
     * ログイン処理。
     *
     * authenticationManager.authenticate() を呼ぶと、
     * Spring Security が内部で UserDetailsServiceImpl.loadUserByUsername() を呼び、
     * DBのハッシュパスワードと入力パスワードを PasswordEncoder.matches() で照合する。
     * 認証失敗時は BadCredentialsException がスローされ、GlobalExceptionHandler が 401 を返す。
     * 認証成功時は例外なく通過するため、そのまま JWT を生成して返す。
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return new AuthResponse(jwtUtil.generateToken(request.email()));
    }
}
