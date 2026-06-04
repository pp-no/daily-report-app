package com.dailyreport.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot アプリケーションのエントリーポイント
 *
 * 【@SpringBootApplication】
 * 以下3つのアノテーションをまとめたもの。
 * - @Configuration     : このクラスをBean定義クラスとして登録
 * - @EnableAutoConfiguration : spring-boot-starter-* の依存関係を自動設定
 * - @ComponentScan     : このパッケージ配下の @Component / @Service 等を自動検出してDI登録
 *
 * 【@EnableScheduling】
 * @Scheduled アノテーションを有効化する。
 * これがないと NotificationScheduler の定時実行が動かない。
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
