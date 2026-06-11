# 作業報告書

| 項目 | 内容 |
|---|---|
| 日付 | - |
| プロジェクト | 日報管理アプリ（DailyReport App） |
| 担当者 | oki |
| フェーズ | Phase 4：REST API動作確認 / Phase 5：JWT認証 / Phase 6：メール通知・定時実行 |

---

## 作業概要

Phase 4のREST API動作確認、Phase 5のJWT認証実装、Phase 6のメール通知・定時実行を実施。curlによるAPIテストとGmailへのメール送信受信まで完了した。

---

## 作業内容

### 1. Phase 4：REST API 動作確認

**目的：** 前回実装したCRUD APIが正常に動作することを確認する

**実施内容：**
- Docker起動（`docker compose up -d`）
- Spring Boot起動（IntelliJ で BackendApplication 実行）
- DBにテストユーザーを直接INSERT
  ```sql
  INSERT INTO users (name, email, password, work_start_time, notification_enabled)
  VALUES ('テストユーザー', 'test@example.com', 'dummy_password', '09:00', true);
  ```
- curlでAPIの動作確認
  - `POST /api/reports` → 日報作成成功（201 Created）
  - `GET /api/reports` → 一覧取得成功
  - `GET /api/reports/{id}` → 詳細取得成功
  - バリデーションエラー（title空）→ `{"message":"タイトルは必須です"}` 返却確認

**成果物：**
- 動作確認完了（ファイル変更なし）

---

### 2. Phase 5：JWT認証実装

**目的：** Spring Security + JWT でログインユーザーのみAPIを利用できるようにする

**実施内容：**
- `build.gradle` に jjwt 依存関係を追加
  ```groovy
  implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
  runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
  runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
  ```
- `application.properties` にJWT設定を追加（secret・expiration）
- JWT関連クラスを作成
  - `JwtUtil.java`：トークン生成・検証
  - `JwtFilter.java`：Authorizationヘッダーからトークンを検証してSecurityContextに認証情報をセット
  - `UserDetailsServiceImpl.java`：メールアドレスでDBからユーザーを取得
- 認証API用DTOを作成
  - `RegisterRequest.java`（name / email / password）
  - `LoginRequest.java`（email / password）
  - `AuthResponse.java`（token）
- `AuthService.java`：ユーザー登録・ログイン処理（BCryptでパスワードハッシュ化）
- `AuthController.java`：`POST /api/auth/register`・`POST /api/auth/login`
- `SecurityConfig.java` を更新
  - JwtFilterをSecurityFilterChainに追加
  - `/api/auth/**` と `/api/reports/public` のみpermitAll、それ以外は認証必須
  - セッションレス設定（STATELESS）
  - CORS設定（`http://localhost:5173` を許可）
- `DailyReportController.java` を更新
  - `@RequestHeader("X-User-Email")` → `@AuthenticationPrincipal UserDetails` に変更
- 動作確認
  - `POST /api/auth/register` → JWTトークン発行確認
  - `Authorization: Bearer <token>` でAPIアクセス成功
  - トークンなしでアクセス → 403 Forbidden 確認

**成果物：**
- `backend/src/main/java/com/dailyreport/backend/security/JwtUtil.java`
- `backend/src/main/java/com/dailyreport/backend/security/JwtFilter.java`
- `backend/src/main/java/com/dailyreport/backend/security/UserDetailsServiceImpl.java`
- `backend/src/main/java/com/dailyreport/backend/api/dto/RegisterRequest.java`
- `backend/src/main/java/com/dailyreport/backend/api/dto/LoginRequest.java`
- `backend/src/main/java/com/dailyreport/backend/api/dto/AuthResponse.java`
- `backend/src/main/java/com/dailyreport/backend/service/AuthService.java`
- `backend/src/main/java/com/dailyreport/backend/api/controller/AuthController.java`
- `backend/src/main/java/com/dailyreport/backend/config/SecurityConfig.java`（更新）
- `backend/src/main/java/com/dailyreport/backend/api/controller/DailyReportController.java`（更新）

---

### 3. Phase 6：メール通知・定時実行

**目的：** 業務開始30分前に前日の日報まとめをメールで自動送信する

**実施内容：**
- `application.properties` にGmail SMTP設定を追加
  ```properties
  spring.mail.host=smtp.gmail.com
  spring.mail.port=587
  spring.mail.username=your-email@example.com
  spring.mail.password=（Googleアプリパスワード）
  spring.mail.properties.mail.smtp.auth=true
  spring.mail.properties.mail.smtp.starttls.enable=true
  ```
- `MailService.java` を作成（`SimpleMailMessage` でメール送信）
- `NotificationScheduler.java` を作成
  - `@Scheduled(cron = "0 * * * * *")` で毎分実行
  - `LocalTime.now() + 30分` に一致する `work_start_time` のユーザーを検索
  - 前日の日報の `summary` が入力済みの場合のみメール送信
- `BackendApplication.java` に `@EnableScheduling` を追加
- デバッグ用に `@Slf4j` ログを追加して対象ユーザー数・送信先をコンソール出力
- 動作確認：`your-email@example.com` へのメール受信を確認

**成果物：**
- `backend/src/main/java/com/dailyreport/backend/service/MailService.java`
- `backend/src/main/java/com/dailyreport/backend/scheduler/NotificationScheduler.java`
- `backend/src/main/java/com/dailyreport/backend/BackendApplication.java`（更新）

---

## 確認事項・懸念点

| 項目 | 内容 |
|---|---|
| Gmailアプリパスワード | `application.properties` に直書きしている。本番環境では環境変数（`${MAIL_PASSWORD}`）に移す必要あり |
| アプリパスワード誤入力 | `ll` → `lj` の誤りで認証失敗。修正済み |
| 401 vs 403 | トークンなしアクセスが401ではなく403を返す。Spring Securityのデフォルト動作。Phase 5完了後に必要に応じて調整 |
| work_start_time の精度 | 分単位で一致するため、テスト時は毎分DBを更新してタイミングを合わせる必要があった |

---

## 次回作業予定

- Phase 7：Reactフロントエンド開発
  - Viteでプロジェクト作成（`npm create vite@latest frontend -- --template react-ts`）
  - ログイン・日報一覧・日報作成・編集画面の実装
  - JWT認証フロー（axios インターセプター）

---

## 所要時間

| 作業 | 時間 |
|---|---|
| Phase 4 動作確認 | 約20分 |
| Phase 5 JWT認証実装・確認 | 約30分 |
| Phase 6 メール通知実装・確認 | 約60分 |
| **合計** | **約110分** |
