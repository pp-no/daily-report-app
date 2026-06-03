package com.dailyreport.backend.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson（JSON変換ライブラリ）の設定
 *
 * 【問題】デフォルトでは LocalDate や LocalDateTime をタイムスタンプ（数値）で出力する。
 * 例: [2024, 6, 1] や 1717200000000
 *
 * 【解決】WRITE_DATES_AS_TIMESTAMPS を無効にすることで、ISO 8601 形式の文字列で出力する。
 * 例: "2024-06-01" や "2024-06-01T09:00:00"
 *
 * これによりフロントエンドが日付をそのまま扱いやすくなる。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
