package com.dailyreport.backend.config;

import com.dailyreport.backend.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 設定
 *
 * 【@EnableWebSecurity】Spring Security を有効化し、このクラスの設定を使う宣言。
 *
 * 【全体の認証フロー】
 * HTTPリクエスト
 *   → JwtFilter（JWTをチェックしてユーザー情報をSecurityContextに格納）
 *   → SecurityFilterChain（認証が必要なURLかチェック）
 *   → Controller
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 【CSRF無効化】JWT認証を使うため不要。
                // CSRFはブラウザのCookieセッションを悪用した攻撃への対策だが、
                // JWTはAuthorizationヘッダーで送るためCSRF攻撃を受けない。
                .csrf(AbstractHttpConfigurer::disable)

                // 【CORS設定】フロントエンド（localhost:5173）からのリクエストを許可する。
                // ブラウザはオリジンが異なるAPIへのリクエストをデフォルトでブロックするため。
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 【セッション無効化】JWTはステートレスなため、サーバー側でセッションを持たない。
                // 毎リクエスト、JWTから認証情報を復元する。
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 【未認証時に401を返す】デフォルトはリダイレクト（302）または403のため明示的に設定。
                // REST APIではリダイレクトは不適切。
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // 【URLごとのアクセス制御】
                // /api/auth/** と /api/reports/public は認証不要。それ以外はJWT必須。
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/reports/public").permitAll()
                        .anyRequest().authenticated()
                )

                // 【JwtFilterの登録】Spring Securityのデフォルト認証フィルターの手前に差し込む。
                // これにより、JWTの検証・ユーザー情報のセットが先に行われる。
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * パスワードハッシュ化アルゴリズム。
     * BCrypt はソルト付きハッシュで、同じパスワードでも毎回異なるハッシュ値になる。
     * これによりレインボーテーブル攻撃に強い。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthService.login() で authenticationManager.authenticate() を呼ぶために必要。
     * Spring Security の認証処理を呼び出す入口となるオブジェクト。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS（Cross-Origin Resource Sharing）設定
     *
     * ブラウザのセキュリティポリシーにより、異なるオリジン（ドメイン・ポート）への
     * リクエストはデフォルトでブロックされる。
     * ここでフロントエンド（localhost:5173）からのアクセスを明示的に許可する。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
