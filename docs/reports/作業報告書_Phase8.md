# 作業報告書

| 項目 | 内容 |
|---|---|
| 日付 | - |
| プロジェクト | daily-report-app |
| 担当者 | - |
| フェーズ | Phase 8：テスト（JUnit・Mockito・MockMvc） |

---

## 作業概要

Spring Boot バックエンドに対してJUnit 5 + Mockito の単体テストおよびMockMvcを使った統合テストを実装した。また、実装漏れが判明した `/api/users/me` エンドポイント（UserController・UserService・DTO）の追加実装と ProfilePage.tsx のAPI接続も完了した。合計7クラス39テストケース（＋1件スキップ）が全通過する状態を確認した。

---

## 作業内容

### 1. テストクラスの新規作成（5ファイル）

**目的：** Phase 8の学習目標であるバックエンドのテスト実装。Serviceレイヤーのビジネスロジック検証とControllerのHTTPレスポンス検証を行う。

**実施内容：**
- Serviceの単体テスト2クラスをMockitoで実装（DBなし・Springコンテキストなし）
- JwtUtilの単体テスト1クラスをReflectionTestUtilsで実装
- Controllerの統合テスト2クラスを`@WebMvcTest` + MockMvcで実装

**成果物：**

| ファイルパス | テスト種別 | テスト数 |
|---|---|---|
| `backend/src/test/.../service/DailyReportServiceTest.java` | Mockito単体テスト | 10件 |
| `backend/src/test/.../service/AuthServiceTest.java` | Mockito単体テスト | 4件 |
| `backend/src/test/.../security/JwtUtilTest.java` | 単体テスト | 4件 |
| `backend/src/test/.../api/controller/DailyReportControllerTest.java` | MockMvc統合テスト | 7件 |
| `backend/src/test/.../api/controller/AuthControllerTest.java` | MockMvc統合テスト | 5件 |
| `backend/src/test/.../service/UserServiceTest.java` | Mockito単体テスト | 5件 |
| `backend/src/test/.../api/controller/UserControllerTest.java` | MockMvc統合テスト | 4件 |

---

### 2. テスト項目の詳細

#### DailyReportServiceTest（10件）

| テストメソッド | 観点 |
|---|---|
| `getMyReports_正常系_日報一覧が返る` | 正常系：ユーザーの日報一覧が取得できる |
| `getMyReports_ユーザーが存在しない場合_例外がスロー` | 異常系：存在しないユーザーで `IllegalArgumentException` |
| `getMyReport_正常系_日報が返る` | 正常系：単件取得ができる |
| `getMyReport_日報が存在しない場合_例外がスロー` | 異常系：存在しないIDで `ReportNotFoundException` |
| `getMyReport_他人の日報にアクセスした場合_例外がスロー` | 異常系：他ユーザーの日報アクセスで `ReportAccessDeniedException` |
| `create_正常系_日報が作成されリポジトリに保存される` | 正常系：日報が作成されsaveが1回呼ばれる |
| `update_正常系_日報の内容が更新される` | 正常系：タイトル・公開設定が反映される |
| `update_他人の日報を更新しようとした場合_例外がスロー` | 異常系：他ユーザーの日報更新でsaveが呼ばれない |
| `delete_正常系_日報が削除される` | 正常系：deleteが1回呼ばれる |
| `delete_他人の日報を削除しようとした場合_例外がスロー` | 異常系：他ユーザーの日報削除でdeleteが呼ばれない |

#### AuthServiceTest（4件）

| テストメソッド | 観点 |
|---|---|
| `register_正常系_JWTトークンが返る` | 正常系：登録後にJWTが返り、パスワードがエンコードされる |
| `register_メールアドレスが重複している場合_例外がスロー` | 異常系：重複メールで `IllegalArgumentException`、saveが呼ばれない |
| `login_正常系_JWTトークンが返る` | 正常系：ログイン後にJWTが返る |
| `login_パスワードが誤っている場合_例外がスロー` | 異常系：認証失敗で `BadCredentialsException` |

#### JwtUtilTest（4件）

| テストメソッド | 観点 |
|---|---|
| `generateToken_生成したトークンからメールアドレスを取り出せる` | 正常系：生成→抽出の往復が正しい |
| `isTokenValid_有効なトークンはtrueを返す` | 正常系：自分で生成したトークンはtrue |
| `isTokenValid_改ざんされたトークンはfalseを返す` | 異常系：末尾に文字を付加した改ざんトークンはfalse |
| `isTokenValid_全く異なる文字列はfalseを返す` | 異常系：JWT形式でない文字列はfalse |

#### DailyReportControllerTest（7件）

| テストメソッド | 観点 |
|---|---|
| `getMyReports_認証済みユーザー_200と日報一覧が返る` | 正常系：`@WithMockUser`でGET → 200+JSONボディ確認 |
| `getMyReports_未認証の場合_401が返る` | 認証：未認証でGET → 401 |
| `getPublicReports_認証なしでも_200と公開日報が返る` | 認可：`permitAll()`エンドポイントに未認証でアクセス可 |
| `createReport_正常系_201と作成された日報が返る` | 正常系：有効なJSONでPOST → 201+作成データ確認 |
| `createReport_必須項目が空の場合_400が返る` | バリデーション：必須項目欠けでPOST → 400、Serviceは呼ばれない |
| `deleteReport_正常系_204が返る` | 正常系：DELETE → 204、deleteが1回呼ばれる |
| `getMyReport_日報が存在しない場合_404が返る` | 例外ハンドリング：`ReportNotFoundException`が404+JSONに変換される |

#### AuthControllerTest（5件）

| テストメソッド | 観点 |
|---|---|
| `register_正常系_201とトークンが返る` | 正常系：有効なJSONでPOST → 201+トークン確認 |
| `register_名前が空の場合_400が返る` | バリデーション：`@NotBlank`違反 → 400、Serviceは呼ばれない |
| `register_メールアドレスの形式が不正な場合_400が返る` | バリデーション：`@Email`違反 → 400 |
| `login_正常系_200とトークンが返る` | 正常系：有効なJSONでPOST → 200+トークン確認 |
| `login_パスワードが空の場合_400が返る` | バリデーション：`@NotBlank`違反 → 400、Serviceは呼ばれない |

#### UserServiceTest（5件）

| テストメソッド | 観点 |
|---|---|
| `getProfile_正常系_ユーザー情報が返る` | 正常系：メールアドレスからユーザー情報が取得できる |
| `getProfile_ユーザーが存在しない場合_例外がスロー` | 異常系：存在しないメールで `IllegalArgumentException` |
| `updateProfile_正常系_ユーザー情報が更新される` | 正常系：名前・メール・業務開始時刻・通知設定が反映されsaveが1回呼ばれる |
| `updateProfile_メールアドレスを別の未使用アドレスに変更できる` | 正常系：未使用メールへの変更はsaveが1回呼ばれる |
| `updateProfile_メールアドレスが他ユーザーと重複する場合_例外がスロー` | 異常系：既存ユーザーのメールへ変更しようとすると `IllegalArgumentException`、saveが呼ばれない |

#### UserControllerTest（4件）

| テストメソッド | 観点 |
|---|---|
| `getProfile_認証済みユーザー_200とプロフィールが返る` | 正常系：`@WithMockUser`でGET → 200+JSONボディ確認 |
| `getProfile_未認証の場合_401が返る` | 認証：未認証でGET → 401 |
| `updateProfile_正常系_200と更新後プロフィールが返る` | 正常系：有効なJSONでPUT → 200+更新後データ確認 |
| `updateProfile_名前が空の場合_400が返る` | バリデーション：`@NotBlank`違反のPUT → 400、Serviceは呼ばれない |

---

### 3. トラブルシューティングと修正

**目的：** テスト実行時に発生したエラーの原因調査と修正。

**問題①：`@MockBean` 非推奨警告（警告30件）**
- 原因：Spring Boot 3.4 で `org.springframework.boot.test.mock.mockito.@MockBean` が非推奨になった
- 対応：`org.springframework.test.context.bean.override.mockito.@MockitoBean` に変更

**問題②：コントローラーテスト全件が 403/401 で失敗**
- 原因：`@WebMvcTest` はコントローラー層のみスキャンするため、`SecurityConfig`（`@Configuration @EnableWebSecurity`）が除外され、Spring Boot デフォルトセキュリティ（CSRF有効・全エンドポイント認証必須）が適用されていた
- 対応：両テストクラスに `@Import({SecurityConfig.class, JwtFilter.class})` を追加して自作セキュリティ設定を明示的にロード

**問題③：未認証テストが期待値401に対して403を返す**
- 原因：Spring Security 6 ではデフォルトで未認証アクセスに 403 を返す（Spring Security 5 以前から変更）
- 対応：`SecurityConfig` に `.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))` を追加
- 備考：この修正は本番APIとしても正しい動作（401=未認証、403=認可不足）に改善している

---

## 確認事項・懸念点

| 項目 | 内容 |
|---|---|
| `BackendApplicationTests` | `@SpringBootTest` は全体コンテキスト起動のため PostgreSQL 接続が必要。`@Disabled` を付けてCI/CD対象から除外済み。Docker Compose 起動後に手動実行すること。 |

---

## 次回作業予定

- **Phase 9：Docker化（マルチステージビルド）**
  - バックエンド用 `Dockerfile`（Gradleビルド → 軽量JREイメージ）
  - フロントエンド用 `Dockerfile`（Node.jsビルド → Nginxで配信）
  - 全サービスを束ねる `docker-compose.yml` の更新（backend / frontend / db）

---

## 所要時間

| 作業 | 時間 |
|---|---|
| テストクラス5ファイルの設計・実装 | 約30分 |
| トラブルシューティング（3件） | 約20分 |
| `/api/users/me` 実装漏れ対応（UserController・UserService・DTO・テスト2クラス・ProfilePage.tsx API接続） | 約30分 |
| **合計** | **約80分** |
