package com.dailyreport.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT認証フィルター
 *
 * 【役割】毎リクエストの「前処理」として JWT を検証し、ユーザー情報を SecurityContext に格納する。
 * Controllerに到達する前に認証状態をセットすることで、@AuthenticationPrincipal が使えるようになる。
 *
 * 【OncePerRequestFilter】1リクエストにつき1回だけ実行されることを保証するフィルターの基底クラス。
 *
 * 【フロー】
 * 1. AuthorizationヘッダーからJWTを取り出す
 * 2. JWTの署名・有効期限を検証する
 * 3. JWTからメールアドレスを取り出してDBでユーザーを確認する
 * 4. 認証情報をSecurityContextに格納する（これ以降 @AuthenticationPrincipal で取れる）
 * 5. 次のフィルターへ処理を渡す
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // "Authorization: Bearer <token>" の形式を想定。ヘッダーがなければ未認証としてスキップ。
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // "Bearer " の7文字を除いてトークン本体だけ取り出す
        String token = authHeader.substring(7);

        // トークンの署名検証・有効期限チェック。不正なら未認証としてスキップ。
        if (!jwtUtil.isTokenValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        // JWTのpayloadからメールアドレスを取り出し、DBでユーザー情報を取得する
        String email = jwtUtil.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 認証オブジェクトを作成してSecurityContextに格納する。
        // これ以降、Controller で @AuthenticationPrincipal を使ってこのユーザー情報を取り出せる。
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 次のフィルター・最終的にControllerへ処理を渡す
        chain.doFilter(request, response);
    }
}
