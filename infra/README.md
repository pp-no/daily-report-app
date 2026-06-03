# daily-report-app インフラ（AWS CDK）

AWS CDK（TypeScript）で管理するインフラ構成です。学習用途のため、**使うときだけデプロイ・使い終わったら削除**する運用を想定しています。

---

## 構成リソース

| リソース | 内容 |
|---|---|
| VPC | 2AZ、NAT Gatewayなし、パブリック＋分離サブネット |
| ECR | `dailyreport-backend` / `dailyreport-frontend` |
| RDS PostgreSQL | バージョン17、t3.micro、分離サブネット |
| Secrets Manager | `dailyreport/db`（DB認証情報）/ `dailyreport/app`（JWT・メール設定） |
| ECS Fargate | バックエンド（Spring Boot:8080）/ フロントエンド（nginx:80） |
| ALB | `/api/*` → バックエンド、デフォルト → フロントエンド |

---

## 開発開始時（デプロイ手順）

> **注意：** CDK コマンドはすべてプロジェクトルートの `infra/` ディレクトリで実行してください。

> **注意：** `cdk destroy` で削除するたびに ECRのイメージ・Secrets Manager・ECSサービス名・ALBのURLがすべてリセットされます。再デプロイ時は必ずステップ1〜5をすべて実施してください。

### 前提条件
- AWS CLI設定済み（`aws configure`）
- Docker起動済み
- CDKブートストラップ済み（初回のみ `cd infra && npx cdk bootstrap`）

### ステップ1：フェーズ1デプロイ（VPC・ECR・RDS・Secrets Manager）

```bash
cd infra
npx cdk deploy -c ecsEnabled=false
```

完了後、出力された ECR URI を確認する。

### ステップ2：Dockerイメージのビルド・push

```bash
# ECRログイン（<AWS_ACCOUNT_ID> は実際のアカウントIDに置き換える）
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com

# バックエンド（infra/ ディレクトリから実行する場合）
docker buildx build --platform linux/amd64 --push -t <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com/dailyreport-backend:latest ../backend

# フロントエンド
docker buildx build --platform linux/amd64 --push -t <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-1.amazonaws.com/dailyreport-frontend:latest ../frontend
```

### ステップ3：Secrets Manager のシークレット更新

AWS コンソール → Secrets Manager → `dailyreport/app` → 「シークレットの値を編集」

| キー | 値 |
|---|---|
| `JWT_SECRET` | `openssl rand -hex 32` で生成した64文字の文字列 |
| `MAIL_USERNAME` | （SES設定後に更新） |
| `MAIL_PASSWORD` | （SES設定後に更新） |

### ステップ4：フェーズ2デプロイ（ECS・ALB）

```bash
cd infra
npx cdk deploy
```

完了後、出力された `AlbUrl` がアプリのURLになる。

### ステップ5：バックエンドを再起動（シークレットを読み込ませる）

ECSサービス名はデプロイのたびに変わるため、以下で確認してから実行する。

```bash
# バックエンドサービス名を確認
aws ecs list-services --cluster dailyreport --query "serviceArns" --output text

# 確認したサービス名で再起動
aws ecs update-service --cluster dailyreport --service <バックエンドサービス名> --force-new-deployment
```

### ステップ6：ALBのURLを確認

ALBのURLもデプロイのたびに変わるため、以下で確認する。

```bash
aws cloudformation describe-stacks --stack-name DailyReport --query "Stacks[0].Outputs[?OutputKey=='AlbUrl'].OutputValue" --output text
```

---

## 開発終了時（削除手順）

**注意：削除するとRDSのデータも消えます。**

```bash
cd infra
npx cdk destroy
```

確認プロンプトに `y` を入力すると全リソースが削除されます。削除完了まで10〜15分程度かかります。

---

## コスト目安

| リソース | 月額概算 | 備考 |
|---|---|---|
| ALB | 約$16〜 | 起動中は固定費用 |
| ECS Fargate（バックエンド） | 約$15〜 | 起動時間に応じた従量課金 |
| ECS Fargate（フロントエンド） | 約$8〜 | 起動時間に応じた従量課金 |
| RDS t3.micro | 無料 | 12ヶ月無料枠 |
| **合計** | **約$40〜/月** | 起動しっぱなしの場合 |

使い終わったら `cdk destroy` で削除するとコストはゼロになります。

---

## CDK コマンド一覧

```bash
npx cdk deploy -c ecsEnabled=false   # フェーズ1（VPC・ECR・RDS・Secrets）
npx cdk deploy                        # フェーズ2（ECS・ALBも含む全リソース）
npx cdk destroy                       # 全リソース削除
npx cdk diff                          # 現在のスタックとの差分確認
npx cdk synth                         # CloudFormationテンプレートの生成
```
