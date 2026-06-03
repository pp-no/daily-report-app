# DailyReport App

日報管理Webアプリケーション

---

## 課題背景

日報を書くだけでは業務の振り返りが習慣になりにくく、翌朝の業務開始時に前日の内容を忘れてしまうことが多い。

そこで、**業務開始30分前に前日の日報まとめをメールで通知**することで、
「書いて終わり」ではなく、毎朝自然に前日を振り返ってから仕事を始める習慣をつけることを目的としたアプリを開発する。

---

## アプリ概要

ユーザーが日々の業務内容を日報として記録・管理し、翌朝の業務開始30分前に前日の日報サマリーをメールで受け取れるWebアプリケーション。

---

## 技術スタック

| 区分 | 技術 |
|---|---|
| バックエンド | Spring Boot 3.5.x / Java 21 |
| フロントエンド | React 19 / TypeScript |
| データベース | PostgreSQL 17 |
| ORM | Spring Data JPA / Flyway |
| 認証 | Spring Security / JWT |
| メール送信 | Spring Mail（Amazon SES） |
| 定時実行 | @Scheduled |
| コンテナ | Docker / Docker Compose |
| CI/CD | GitHub Actions |
| クラウド | AWS ECS / RDS / SES |

---

## 機能一覧

### ユーザー機能
- ユーザー登録・ログイン・ログアウト
- プロフィール編集（名前・メールアドレス・業務開始時刻）
- パスワード変更

### 日報機能
- 日報の作成・編集・削除・一覧表示
- 日報の入力欄は固定3セクション
  - 今日やったこと
  - 明日やること
  - 所感（任意）
- まとめ欄（メール通知に使用）
- 日報の公開・非公開設定
- 他ユーザーの公開日報を閲覧

### 通知機能
- 業務開始時刻の30分前に前日の日報の**まとめ欄**をメール送信
- メール通知のON/OFF設定
- プロフィール画面から今すぐ送信（任意のタイミングで動作確認可能）

---

## プロジェクト構成

```
daily-report-app/
├── backend/                        # Spring Boot
│   └── src/main/java/
│       ├── api/controller/         # HTTPリクエストの受け口
│       ├── api/dto/                # リクエスト・レスポンスの型定義
│       ├── domain/entity/          # DBテーブルに対応するエンティティ
│       ├── domain/repository/      # DBアクセス（SQL自動生成）
│       ├── service/                # ビジネスロジック
│       ├── security/               # JWT認証フィルター
│       ├── config/                 # セキュリティ・CORS設定
│       └── scheduler/              # メール通知の定時実行
├── frontend/                       # React（TypeScript）
│   └── src/
│       ├── api/                    # axiosクライアント設定
│       ├── components/             # 共通コンポーネント（Layout、Sidebar等）
│       ├── hooks/                  # カスタムフック（useReports、useIsMobile等）
│       ├── pages/                  # 各画面コンポーネント
│       └── types/                  # TypeScript型定義
├── docker-compose.yml              # ローカル開発用（PostgreSQLのみ）
├── CURRICULUM.md                   # 学習カリキュラム
└── README.md
```

---

## 画面一覧

| 画面 | パス | 認証 |
|---|---|---|
| ログイン | /login | 不要 |
| ユーザー登録 | /register | 不要 |
| 日報一覧 | /reports | 必要 |
| 日報作成 | /reports/new | 必要 |
| 日報編集 | /reports/:id/edit | 必要 |
| 公開日報一覧 | /public | 必要 |
| プロフィール設定 | /settings | 必要 |

---

## APIエンドポイント

| メソッド | パス | 説明 | 認証 |
|---|---|---|---|
| POST | /api/auth/register | ユーザー登録 | 不要 |
| POST | /api/auth/login | ログイン | 不要 |
| GET | /api/reports | 自分の日報一覧 | 必要 |
| POST | /api/reports | 日報作成 | 必要 |
| GET | /api/reports/{id} | 日報詳細 | 必要 |
| PUT | /api/reports/{id} | 日報更新 | 必要 |
| DELETE | /api/reports/{id} | 日報削除 | 必要 |
| GET | /api/reports/public | 公開日報一覧 | 必要 |
| GET | /api/users/me | プロフィール取得 | 必要 |
| PUT | /api/users/me | プロフィール更新 | 必要 |
| POST | /api/notifications/send | 通知メールを今すぐ送信 | 必要 |

---

## セットアップ

### 必要ツール

- JDK 21
- Node.js 20 以上
- Docker / Docker Compose

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd daily-report-app
```

### 2. PostgreSQL の起動

```bash
docker compose up -d
```

### 3. バックエンドの設定

`application.properties.example` をコピーして実際の値を入力する。

```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
```

`application.properties` を編集：

```properties
# DB接続（docker-compose.ymlの設定に合わせる）
spring.datasource.url=jdbc:postgresql://localhost:5432/dailyreport
spring.datasource.username=user
spring.datasource.password=password

# JWT（任意の256bit以上の文字列）
jwt.secret=your-secret-key

# Gmail SMTP（Googleアカウントのアプリパスワードを使用）
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-password
```

### 4. バックエンドの起動

```bash
cd backend
./gradlew bootRun
```

### 5. フロントエンドの設定

```bash
cp frontend/.env.example frontend/.env
```

`.env` を確認（デフォルトのままでOK）：

```
VITE_API_URL=http://localhost:8080
```

### 6. フロントエンドの起動

```bash
cd frontend
npm install
npm run dev
```

### 7. ブラウザでアクセス

```
http://localhost:5173
```

---

## 通知メールの仕組み

### 自動通知（スケジューラー）

```
毎分 @Scheduled が実行される（JST基準）
        ↓
「現在時刻 + 30分 = 業務開始時刻」のユーザーを検索
        ↓
前日の日報のまとめ欄が入力済みであれば
        ↓
Amazon SES でメール送信
```

例：業務開始時刻を `09:00` に設定している場合、毎朝 `08:30` にメールが届く。

### 即時送信

プロフィール画面の「今すぐ送信」ボタンから任意のタイミングで送信できる。昨日の日報が存在しない場合はダミーテキストで送信される。

---

## 開発進捗

| Phase | 内容 | 状態 |
|---|---|---|
| Phase 1 | 開発環境セットアップ | ✅ 完了 |
| Phase 2 | Spring Boot基礎（DI・レイヤードアーキテクチャ） | ✅ 完了 |
| Phase 3 | データベース設計・JPA・Flyway | ✅ 完了 |
| Phase 4 | REST API開発・バリデーション・例外ハンドリング | ✅ 完了 |
| Phase 5 | JWT認証・Spring Security | ✅ 完了 |
| Phase 6 | メール通知・@Scheduled定時実行 | ✅ 完了 |
| Phase 7 | Reactフロントエンド開発（レスポンシブ対応） | ✅ 完了 |
| Phase 8 | テスト（JUnit・Mockito・MockMvc） | 🔜 未着手 |
| Phase 9 | Docker化（マルチステージビルド） | ✅ 完了 |
| Phase 10 | AWSデプロイ（ECS・RDS・SES・GitHub Actions） | ✅ 完了 |

---

## 注意事項

- `application.properties` にはDBパスワード・JWTシークレット等が含まれるため **Gitにコミットしない**（`.gitignore` で除外済み）
- 本番環境の機密情報（JWT_SECRET・MAIL_USERNAME・MAIL_PASSWORD）は AWS Secrets Manager（`dailyreport/app`）で管理する
