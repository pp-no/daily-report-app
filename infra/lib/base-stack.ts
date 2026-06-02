import * as cdk from 'aws-cdk-lib/core';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from 'constructs';

// AppStackから参照できるようにpublicで公開する
export class BaseStack extends cdk.Stack {
  public readonly vpc: ec2.Vpc;
  public readonly backendRepo: ecr.Repository;
  public readonly frontendRepo: ecr.Repository;
  public readonly db: rds.DatabaseInstance;
  public readonly dbSecret: secretsmanager.Secret;
  public readonly appSecret: secretsmanager.Secret;
  public readonly dbSg: ec2.SecurityGroup;

  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // VPC（パブリックサブネット＋分離サブネット、NAT Gatewayなしでコスト削減）
    this.vpc = new ec2.Vpc(this, 'Vpc', {
      maxAzs: 2,
      natGateways: 0,
      subnetConfiguration: [
        { name: 'Public', subnetType: ec2.SubnetType.PUBLIC, cidrMask: 24 },
        { name: 'Isolated', subnetType: ec2.SubnetType.PRIVATE_ISOLATED, cidrMask: 24 },
      ],
    });

    // ECR リポジトリ（スタック削除時にリポジトリごと削除）
    this.backendRepo = new ecr.Repository(this, 'BackendRepo', {
      repositoryName: 'dailyreport-backend',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      emptyOnDelete: true,
    });

    this.frontendRepo = new ecr.Repository(this, 'FrontendRepo', {
      repositoryName: 'dailyreport-frontend',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      emptyOnDelete: true,
    });

    // DB パスワードを Secrets Manager で自動生成
    this.dbSecret = new secretsmanager.Secret(this, 'DbSecret', {
      secretName: 'dailyreport/db',
      generateSecretString: {
        secretStringTemplate: JSON.stringify({ username: 'dailyreport' }),
        generateStringKey: 'password',
        excludePunctuation: true,
        passwordLength: 32,
      },
    });

    // アプリシークレット（JWT・メール設定）
    // デプロイ後に AWS コンソールまたは CLI で実際の値に更新すること
    this.appSecret = new secretsmanager.Secret(this, 'AppSecret', {
      secretName: 'dailyreport/app',
      secretStringValue: cdk.SecretValue.unsafePlainText(JSON.stringify({
        JWT_SECRET: 'REPLACE_ME',
        MAIL_USERNAME: 'REPLACE_ME',
        MAIL_PASSWORD: 'REPLACE_ME',
      })),
    });

    // RDS セキュリティグループ（AppStack の BackendSg からの接続のみ許可）
    this.dbSg = new ec2.SecurityGroup(this, 'DbSg', {
      vpc: this.vpc,
      description: 'RDS security group - allows access from backend ECS only',
    });

    // RDS PostgreSQL（分離サブネット、シングルAZ、暗号化あり）
    this.db = new rds.DatabaseInstance(this, 'Db', {
      engine: rds.DatabaseInstanceEngine.postgres({
        version: rds.PostgresEngineVersion.VER_17,
      }),
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      vpc: this.vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      securityGroups: [this.dbSg],
      databaseName: 'dailyreport',
      credentials: rds.Credentials.fromSecret(this.dbSecret),
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      deletionProtection: false,
      multiAz: false,
      storageEncrypted: true,
      backupRetention: cdk.Duration.days(0),
    });

    // ECR リポジトリの URI を出力（GitHub Actions の設定に使用）
    new cdk.CfnOutput(this, 'BackendEcrUri', {
      value: this.backendRepo.repositoryUri,
      description: 'バックエンドECRリポジトリURI',
    });

    new cdk.CfnOutput(this, 'FrontendEcrUri', {
      value: this.frontendRepo.repositoryUri,
      description: 'フロントエンドECRリポジトリURI',
    });
  }
}
