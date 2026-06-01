package com.dailyreport.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest は全体コンテキストを起動するため実際のDB接続（PostgreSQL）が必要。
// ローカル環境では docker compose up で DB を起動してから実行すること。
@SpringBootTest
@Disabled("PostgreSQL接続が必要なため通常のテスト実行では除外。docker compose up 後に手動で実行すること。")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
