package com.dailyreport.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT（JSON Web Token）ユーティリティ
 *
 * 【JWTとは】ヘッダー.ペイロード.署名 の3パートからなるトークン。
 * ペイロードにユーザー情報（メールアドレス等）を埋め込み、
 * 署名で改ざんを検出できる。サーバー側でセッションを保持しなくていい。
 *
 * 【@Value("${jwt.secret}")】application.properties の jwt.secret の値を注入する。
 * 【@Value("${jwt.expiration:86400000}")】設定がなければ86400000ミリ秒（24時間）をデフォルトにする。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // デフォルト 86400000ms = 24時間
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * JWTを生成する。
     * subject にメールアドレスを埋め込み、発行日時・有効期限・署名を付与する。
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** JWTのペイロードからメールアドレス（subject）を取り出す。 */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * JWTの有効性を検証する。
     * 署名が不正、有効期限切れ、形式不正のいずれかで false を返す。
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /** JWTを解析してペイロード（Claims）を取り出す。署名検証も行われる。 */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * シークレットキーを HMAC-SHA 形式の SecretKey オブジェクトに変換する。
     * application.properties の jwt.secret は256bit（32文字）以上が必要。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
