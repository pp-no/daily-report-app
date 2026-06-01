package com.dailyreport.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil の単体テスト。
 * Spring コンテキストなしで @Value フィールドを ReflectionTestUtils で注入して検証する。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // @Value で注入されるフィールドをテスト用の値で直接セット
        // HS256 には 256bit（32バイト）以上のキーが必要
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-must-be-at-least-256bits-long-for-hs256-algorithm-ok");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24時間（ミリ秒）
    }

    @Test
    void generateToken_生成したトークンからメールアドレスを取り出せる() {
        String token = jwtUtil.generateToken("test@example.com");

        String extractedEmail = jwtUtil.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void isTokenValid_有効なトークンはtrueを返す() {
        String token = jwtUtil.generateToken("test@example.com");

        boolean result = jwtUtil.isTokenValid(token);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_改ざんされたトークンはfalseを返す() {
        // 末尾に余分な文字を付加してトークンを改ざん
        String tamperedToken = jwtUtil.generateToken("test@example.com") + "tampered";

        boolean result = jwtUtil.isTokenValid(tamperedToken);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_全く異なる文字列はfalseを返す() {
        boolean result = jwtUtil.isTokenValid("not.a.valid.jwt");

        assertThat(result).isFalse();
    }
}
