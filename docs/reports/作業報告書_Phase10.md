# 作業報告書

| 項目 | 内容 |
|---|---|
| 日付 | - |
| プロジェクト | daily-report-app |
| 担当者 | - |
| フェーズ | Phase 10：AWSデプロイ・CI/CD構築 |

---

## 作業概要

AWS CDK を使って VPC・ECR・RDS・ECS Fargate・ALB・Secrets Manager のインフラをコードで構築し、本番環境へのデプロイを完了した。デプロイ過程でアーキテクチャ設計・CPUアーキテクチャ・nginx設定・JWTシークレット強度の4つの問題が発生し、それぞれ調査・解決した。最後に GitHub Actions による CI/CD パイプラインを構築した。

---

## 作業内容

### 1. AWS環境のセットアップ

**実施内容：**
- AWSアカウント作成
- IAMユーザー（`NO_WORK_IAM`）の作成（管理者権限付与）
- AWS CLI のインストール・設定（`aws configure`）
- AWS CDK のインストール（`npm install -g aws-cdk`）
- MCP サーバー（`awslabs.aws-iac-mcp-server`）を `.mcp.json` に設定

**成果物：**
- `.mcp.json`（新規、`.gitignore` に追加）

---

### 2. CDK インフラ構築（`infra/` ディレクトリ）

**目的：** インフラをコード（TypeScript）で管理し、再現性を確保する

**構成したAWSリソース：**

| リソース | 設定内容 |
|---|---|
| VPC | 2AZ、NAT Gatewayなし（コスト削減）、パブリック＋分離サブネット |
| ECR | `dailyreport-backend` / `dailyreport-frontend` の2リポジトリ |
| Secrets Manager | `dailyreport/db`（DBパスワード自動生成）/ `dailyreport/app`（JWT・メール設定） |
| RDS PostgreSQL | バージョン17、t3.micro、分離サブネット、暗号化あり |
| ECS Fargate | クラスター `dailyreport`、バックエンド（512CPU/1024MB）、フロントエンド（256CPU/512MB） |
| ALB | インターネット向け、`/api/*` → バックエンド、デフォルト → フロントエンド |
| セキュリティグループ | ALB(80) → フロントエンド(80) / ALB(80) → バックエンド(8080) → RDS(5432) |

**デプロイを2フェーズに分けた理由：**

ECSタスクはDockerイメージをECRから取得するため、先にECRが存在していないとECSタスク定義が作れない。そのため以下の順序で実施した。

| フェーズ | コマンド | 作成リソース |
|---|---|---|
| フェーズ1 | `cdk deploy -c ecsEnabled=false` | VPC・ECR・RDS・Secrets Manager |
| フェーズ2 | `cdk deploy` | ECS・ALB（フェーズ1のリソースを参照） |

フェーズ1でECRのURIを出力し、そのURIにDockerイメージをpushしてからフェーズ2を実行した。

**成果物：**
- `infra/bin/infra.ts`（新規）
- `infra/lib/infra-stack.ts`（新規）

---

### 3. Dockerイメージのビルド・ECRへのpush

**実施内容：**
- ECRにログイン
- バックエンド・フロントエンドのDockerイメージをビルドしてECRにpush

**使用コマンド：**
```bash
# ECRログイン
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com

# バックエンドイメージのビルド・push
docker buildx build --platform linux/amd64 --push \
  -t <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com/dailyreport-backend:latest \
  ./backend

# フロントエンドイメージのビルド・push
docker buildx build --platform linux/amd64 --push \
  -t <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com/dailyreport-frontend:latest \
  ./frontend
```

---

### 4. Secrets Manager のシークレット更新

フェーズ1で作成した `dailyreport/app` シークレットの値が `REPLACE_ME` のままのため、以下の値を更新した。

| キー | 内容 |
|---|---|
| `JWT_SECRET` | `openssl rand -hex 32` で生成した64文字のランダム文字列 |
| `MAIL_USERNAME` | 別途対応（SES設定時） |
| `MAIL_PASSWORD` | 別途対応（SES設定時） |

更新後、バックエンドのECSサービスを再起動して新しいシークレットを読み込ませた。

---

### 5. GitHub Actions CI/CD パイプライン構築

**目的：** `main` ブランチへの push を起点にビルド・デプロイを自動化する

**パイプラインの流れ：**
```
git push (main) → GitHub Actions 起動
  → AWS認証
  → ECRログイン
  → バックエンドイメージのビルド・ECRpush
  → フロントエンドイメージのビルド・ECRpush
  → ECS バックエンドサービス 再デプロイ
  → ECS フロントエンドサービス 再デプロイ
```

**GitHub Secrets に登録した値：**

| シークレット名 | 用途 |
|---|---|
| `AWS_ACCESS_KEY_ID` | GitHub Actions専用IAMユーザーのアクセスキー |
| `AWS_SECRET_ACCESS_KEY` | 同上のシークレットキー |

GitHub Actions専用のIAMユーザー（`github-actions-deployer`）を最小権限で作成し、ECRへのpushとECSサービスの更新のみ許可した。

**成果物：**
- `.github/workflows/deploy.yml`（新規）

---

## ハマったポイントと解決策

### ① CDK スタックのサイクル依存エラー

**問題：**
当初 `BaseStack`（VPC・RDS等）と `AppStack`（ECS・ALB等）の2スタックに分割して設計した。しかし、RDSのセキュリティグループ（`BaseStack`内）にバックエンドECSのセキュリティグループ（`AppStack`内）からの接続を許可するルールを追加しようとしたところ、2つのスタックが互いを参照するサイクル依存エラーが発生した。

```
Error: Cycle detected: AppStack -> BaseStack -> AppStack
```

**解決策：**
`BaseStack` と `AppStack` を1つの `InfraStack` に統合した。CDKのコンテキスト変数 `ecsEnabled` で ECS/ALB リソースの作成有無を切り替えることで、2フェーズデプロイの要件は維持した。

```typescript
const ecsEnabled = this.node.tryGetContext('ecsEnabled') !== 'false';
// ...
if (!ecsEnabled) return; // ECS/ALBはこの後に定義
```

---

### ② Apple Silicon（ARM64）と ECS Fargate（AMD64）のアーキテクチャ不一致

**問題：**
開発機が Apple Silicon（M系チップ、ARM64アーキテクチャ）のため、通常の `docker build` コマンドでビルドするとARM64イメージが作成される。しかしECS Fargateは `linux/amd64`（x86_64）のみ対応しているため、コンテナが起動しなかった。

**エラーの兆候：**
- ECSタスクが起動直後に終了する
- CloudWatch Logsにアーキテクチャ関連のエラーが記録される

**解決策：**
`docker buildx` コマンドに `--platform linux/amd64` オプションを指定してビルドすることで、AMD64イメージを明示的に作成した。

```bash
# NG（Apple SiliconではARM64イメージになる）
docker build -t myimage:latest .

# OK（AMD64イメージを明示的にビルド）
docker buildx build --platform linux/amd64 -t myimage:latest .
```

---

### ③ フロントエンド ECS タスクの起動失敗（nginx ホスト解決エラー）

**問題：**
フロントエンドのECSタスクが16回連続で失敗し、CloudFormationのデプロイが完了しなかった。CloudWatch Logsを確認したところ以下のエラーが発生していた。

```
nginx: [emerg] host not found in upstream "backend" in /etc/nginx/conf.d/default.conf:9
```

**原因：**
Phase 9でローカル開発用に作成した `nginx.conf` に以下のプロキシ設定が含まれていた。

```nginx
location /api/ {
    proxy_pass http://backend:8080;  # ← Docker Composeのサービス名
}
```

Docker Compose環境では `backend` というホスト名でコンテナ間通信ができるが、ECS環境ではDocker Composeのネットワークが存在しないため、`backend` というホストが解決できずnginxがエラーで起動しなかった。

**AWS環境でのリクエストフロー（正しい理解）：**
```
ブラウザ → ALB → /api/* → バックエンドECS
                → その他 → フロントエンドECS（nginxが静的ファイルを返す）
```

AWS環境ではALBがルーティングを担うため、フロントエンドのnginxにプロキシ設定は不要。

**解決策：**
- `frontend/nginx.conf`（本番用）からプロキシ設定を削除
- `frontend/nginx.dev.conf`（ローカル開発用）を新規作成してプロキシ設定を移行
- `docker-compose.yml` でローカル起動時は `nginx.dev.conf` をマウントするよう変更

```yaml
# docker-compose.yml（ローカル開発用）
frontend:
  volumes:
    - ./frontend/nginx.dev.conf:/etc/nginx/conf.d/default.conf
```

---

### ④ JWT シークレットの強度不足エラー

**問題：**
Secrets Manager の `JWT_SECRET` を更新してバックエンドを再起動したが、ユーザー登録時にエラーが発生した。CloudWatch Logsに以下のエラーが記録されていた。

```
WeakKeyException: The specified key byte array is 80 bits which is not secure enough
for any JWT HMAC-SHA algorithm.
The JWT JWA Specification (RFC 7518, Section 3.2) states that keys used with
HMAC-SHA algorithms MUST have a size >= 256 bits.
```

**原因：**
設定した `JWT_SECRET` が10文字程度（80 bits）と短すぎた。JWTのHMAC-SHA256アルゴリズムには最低256 bits（32バイト以上）のキーが必要。

**解決策：**
`openssl rand -hex 32` で64文字（256 bits）のランダム文字列を生成して再設定した。

```bash
openssl rand -hex 32
# 例: a3f8c2e1d5b4a7f9e0c3d6b8a2e5f1c4d7b0a3e6f9c2d5b8a1e4f7c0d3b6a9
```

---

## 最終構成

```
インターネット
    ↓ HTTP:80
ALB（Application Load Balancer）
    ↓ /api/*              ↓ それ以外
バックエンドECS          フロントエンドECS
（Spring Boot:8080）     （nginx:80 / React）
    ↓
RDS PostgreSQL（分離サブネット）

Secrets Manager
  ├── dailyreport/db（DBユーザー名・パスワード）
  └── dailyreport/app（JWT_SECRET・MAIL_USERNAME・MAIL_PASSWORD）
```

---

## ハマったポイントと解決策（追加）

### ⑤ cdk destroy 後の再デプロイ時に ECR イメージが消える

**問題：**
`cdk destroy` で全リソースを削除後、`cdk deploy` で再構築したところECSタスクが起動しなかった。

```
CannotPullContainerError: failed to resolve ref ...dailyreport-backend:latest: not found
```

**原因：**
`cdk destroy` 時に ECR リポジトリも削除されるため、イメージも消える。`cdk deploy` でECRリポジトリは再作成されるが、イメージは空の状態。

**解決策：**
`cdk deploy` 後に毎回 ECR へのイメージ push が必要。`infra/README.md` にステップとして明記した。

---

### ⑥ GitHub Actions で ECSサービス名のハードコードが問題に

**問題：**
`cdk destroy` → `cdk deploy` のたびにECSサービス名末尾のランダム文字列が変わる。`deploy.yml` にハードコードしていたため、再デプロイ後にCI/CDが失敗した。

```
ServiceNotFoundException: ... DailyReport-BackendService7A4224EE-NAKSUgXU6UoV
```

**解決策：**
サービス名をAWS CLIで動的に取得するよう変更した。

```bash
BACKEND_SERVICE=$(aws ecs list-services --cluster dailyreport \
  --query "serviceArns[?contains(@, 'BackendService')]" \
  --output text | awk -F'/' '{print $NF}')
```

合わせて `github-actions-deployer` IAMユーザーに `ecs:ListServices` 権限を追加した。

---

### ⑦ スケジューラーのタイムゾーン問題

**問題：**
ECSコンテナはUTCで動作するため、`LocalTime.now()` がUTC時刻を返す。ユーザーがJSTで設定した業務開始時刻と一致せずメール通知が届かなかった。

**解決策：**
`LocalTime.now()` と `LocalDate.now()` にJSTタイムゾーンを指定した。

```java
ZoneId jst = ZoneId.of("Asia/Tokyo");
LocalTime now = LocalTime.now(jst).withSecond(0).withNano(0);
LocalDate yesterday = LocalDate.now(jst).minusDays(1);
```

---

### ⑧ SES メール送信が FROM アドレス未設定で拒否される

**問題：**
スケジューラーおよびテスト送信でメールが届かず、CloudWatch Logs に以下のエラーが記録されていた。

```
554 Message rejected: Email address is not verified.
The following identities failed the check in region AP-NORTHEAST-1:
root@ip-10-0-1-25.ap-northeast-1.compute.internal
```

**原因：**
`MailService` で `message.setFrom()` を呼んでいなかったため、FROM アドレスがデフォルトのコンテナホスト名（`root@ip-10-0-1-25...`）になっていた。SES はこのアドレスを未検証として拒否した。

**解決策：**
FROM アドレスに TO（受信者）と同じユーザーのメールアドレスを設定した。このアプリは自分宛に通知を送る仕組みのため、TO = FROM で成立する。

```java
message.setFrom(to);  // 登録メールアドレスをFROMにも使用
message.setTo(to);
```

---

### ⑨ SES から送信したメールが Gmail の迷惑メールに振り分けられる

**問題：**
FROM アドレス修正後にメールは届くようになったが、受信トレイではなく迷惑メールフォルダに入っていた。

**原因：**
SES サンドボックスモードで送信したメールは送信ドメインの信頼性がまだ低く、Gmail にスパム判定される。

**解決策：**
迷惑メールフォルダで「迷惑メールではない」をクリックすることで Gmail が学習し、以降は受信トレイに届くようになった。

---

### ⑩ 即時送信機能の追加

**背景：**
スケジューラーによる通知は「業務開始30分前」にしか動作しないため、動作確認に時間がかかった。

**対応：**
プロフィール画面に「今すぐ送信」ボタンを追加し、任意のタイミングで通知メールを送信できる機能を実装した。

- バックエンド：`POST /api/notifications/test` エンドポイントを新規作成
- フロントエンド：プロフィール画面の通知設定カードにボタンを追加
- 昨日の日報が存在しない場合はダミーテキストで送信

---

## 確認事項・懸念点

| 項目 | 内容 |
|---|---|
| HTTPS対応 | 現在は HTTP のみ。本番運用にはACM証明書取得とALBのHTTPS設定が必要（独自ドメイン要） |
| コスト | RDS t3.micro・ECS Fargate（最小構成）で稼働中。学習用途のため使用時のみデプロイする運用 |

---

## 所要時間

| 作業 | 時間 |
|---|---|
| AWS環境セットアップ（アカウント・IAM・CLI・CDK） | 約30分 |
| CDKインフラ設計・実装 | 約60分 |
| デプロイトラブルシュート（4つのハマりポイント解決） | 約120分 |
| Secrets Manager更新・動作確認 | 約20分 |
| GitHub Actions CI/CD構築 | 約20分 |
| SESセットアップ・メール通知動作確認 | 約30分 |
| CI/CDエラー対応・タイムゾーン修正 | 約30分 |
| **合計** | **約310分** |
