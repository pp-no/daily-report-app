import * as cdk from 'aws-cdk-lib/core';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export class InfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // ecsEnabled=false のときは VPC・ECR・RDS・Secrets のみデプロイする
    // フェーズ1: cdk deploy -c ecsEnabled=false
    // フェーズ2: cdk deploy  （デフォルト true でECS/ALBも含む）
    const ecsEnabled = this.node.tryGetContext('ecsEnabled') !== 'false';

    // ===== VPC =====
    // NAT Gateway なしでコスト削減。ECS は Public サブネットで公開IPを持ちECRにアクセスする。
    const vpc = new ec2.Vpc(this, 'Vpc', {
      maxAzs: 2,
      natGateways: 0,
      subnetConfiguration: [
        { name: 'Public', subnetType: ec2.SubnetType.PUBLIC, cidrMask: 24 },
        { name: 'Isolated', subnetType: ec2.SubnetType.PRIVATE_ISOLATED, cidrMask: 24 },
      ],
    });

    // ===== ECR リポジトリ =====
    const backendRepo = new ecr.Repository(this, 'BackendRepo', {
      repositoryName: 'dailyreport-backend',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      emptyOnDelete: true,
    });

    const frontendRepo = new ecr.Repository(this, 'FrontendRepo', {
      repositoryName: 'dailyreport-frontend',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      emptyOnDelete: true,
    });

    // ===== Secrets Manager =====

    // DB パスワードを自動生成（平文で管理しない）
    const dbSecret = new secretsmanager.Secret(this, 'DbSecret', {
      secretName: 'dailyreport/db',
      generateSecretString: {
        secretStringTemplate: JSON.stringify({ username: 'dailyreport' }),
        generateStringKey: 'password',
        excludePunctuation: true,
        passwordLength: 32,
      },
    });

    // アプリシークレット（デプロイ後にコンソールまたは CLI で実際の値に更新すること）
    const appSecret = new secretsmanager.Secret(this, 'AppSecret', {
      secretName: 'dailyreport/app',
      secretStringValue: cdk.SecretValue.unsafePlainText(JSON.stringify({
        JWT_SECRET: 'REPLACE_ME',
        MAIL_USERNAME: 'REPLACE_ME',
        MAIL_PASSWORD: 'REPLACE_ME',
      })),
    });

    // ===== RDS セキュリティグループ =====
    const dbSg = new ec2.SecurityGroup(this, 'DbSg', {
      vpc,
      description: 'RDS - allows 5432 from backend ECS only',
    });

    // ===== RDS PostgreSQL =====
    // 分離サブネット（インターネット経路なし）に配置し、セキュリティグループで制限
    const db = new rds.DatabaseInstance(this, 'Db', {
      engine: rds.DatabaseInstanceEngine.postgres({
        version: rds.PostgresEngineVersion.VER_17,
      }),
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      securityGroups: [dbSg],
      databaseName: 'dailyreport',
      credentials: rds.Credentials.fromSecret(dbSecret),
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      deletionProtection: true,
      multiAz: false,
      storageEncrypted: true,
      backupRetention: cdk.Duration.days(7),
    });

    // ===== ECR URI 出力（フェーズ1で確認できるようにECS無効時も出力）=====
    new cdk.CfnOutput(this, 'BackendEcrUri', {
      value: backendRepo.repositoryUri,
      description: 'バックエンドECRリポジトリURI（イメージpushに使用）',
    });

    new cdk.CfnOutput(this, 'FrontendEcrUri', {
      value: frontendRepo.repositoryUri,
      description: 'フロントエンドECRリポジトリURI（イメージpushに使用）',
    });

    // ===== ECS / ALB（フェーズ2のみ）=====
    if (!ecsEnabled) return;

    // セキュリティグループ
    const albSg = new ec2.SecurityGroup(this, 'AlbSg', {
      vpc,
      description: 'ALB - allows HTTP from internet',
    });
    albSg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'HTTP from internet');

    const backendSg = new ec2.SecurityGroup(this, 'BackendSg', {
      vpc,
      description: 'Backend ECS - allows 8080 from ALB',
    });
    backendSg.addIngressRule(albSg, ec2.Port.tcp(8080), 'From ALB');

    const frontendSg = new ec2.SecurityGroup(this, 'FrontendSg', {
      vpc,
      description: 'Frontend ECS - allows 80 from ALB',
    });
    frontendSg.addIngressRule(albSg, ec2.Port.tcp(80), 'From ALB');

    // RDS へのアクセスはバックエンド ECS からのみ許可
    dbSg.addIngressRule(backendSg, ec2.Port.tcp(5432), 'From backend ECS');

    // ECS クラスター
    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc,
      clusterName: 'dailyreport',
    });

    // バックエンド タスク定義
    const backendTaskDef = new ecs.FargateTaskDefinition(this, 'BackendTaskDef', {
      cpu: 512,
      memoryLimitMiB: 1024,
    });

    backendTaskDef.addContainer('backend', {
      image: ecs.ContainerImage.fromEcrRepository(backendRepo, 'latest'),
      portMappings: [{ containerPort: 8080 }],
      environment: {
        SPRING_DATASOURCE_URL: `jdbc:postgresql://${db.dbInstanceEndpointAddress}:5432/dailyreport`,
        SPRING_MAIL_HOST: `email-smtp.${this.region}.amazonaws.com`,
        SPRING_MAIL_PORT: '587',
        SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH: 'true',
        SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE: 'true',
      },
      secrets: {
        // Secrets Manager から実行時に取得（平文でタスク定義に残らない）
        SPRING_DATASOURCE_USERNAME: ecs.Secret.fromSecretsManager(dbSecret, 'username'),
        SPRING_DATASOURCE_PASSWORD: ecs.Secret.fromSecretsManager(dbSecret, 'password'),
        JWT_SECRET: ecs.Secret.fromSecretsManager(appSecret, 'JWT_SECRET'),
        SPRING_MAIL_USERNAME: ecs.Secret.fromSecretsManager(appSecret, 'MAIL_USERNAME'),
        SPRING_MAIL_PASSWORD: ecs.Secret.fromSecretsManager(appSecret, 'MAIL_PASSWORD'),
      },
      logging: ecs.LogDrivers.awsLogs({
        streamPrefix: 'backend',
        logGroup: new logs.LogGroup(this, 'BackendLogGroup', {
          logGroupName: '/ecs/dailyreport-backend',
          removalPolicy: cdk.RemovalPolicy.DESTROY,
          retention: logs.RetentionDays.ONE_WEEK,
        }),
      }),
    });

    // フロントエンド タスク定義
    const frontendTaskDef = new ecs.FargateTaskDefinition(this, 'FrontendTaskDef', {
      cpu: 256,
      memoryLimitMiB: 512,
    });

    frontendTaskDef.addContainer('frontend', {
      image: ecs.ContainerImage.fromEcrRepository(frontendRepo, 'latest'),
      portMappings: [{ containerPort: 80 }],
      logging: ecs.LogDrivers.awsLogs({
        streamPrefix: 'frontend',
        logGroup: new logs.LogGroup(this, 'FrontendLogGroup', {
          logGroupName: '/ecs/dailyreport-frontend',
          removalPolicy: cdk.RemovalPolicy.DESTROY,
          retention: logs.RetentionDays.ONE_WEEK,
        }),
      }),
    });

    // ALB
    const alb = new elbv2.ApplicationLoadBalancer(this, 'Alb', {
      vpc,
      internetFacing: true,
      securityGroup: albSg,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
    });

    // バックエンド ECS サービス（パブリックサブネットで公開IP付与）
    const backendService = new ecs.FargateService(this, 'BackendService', {
      cluster,
      taskDefinition: backendTaskDef,
      desiredCount: 1,
      securityGroups: [backendSg],
      assignPublicIp: true,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
    });

    const backendTg = new elbv2.ApplicationTargetGroup(this, 'BackendTg', {
      vpc,
      port: 8080,
      protocol: elbv2.ApplicationProtocol.HTTP,
      targets: [backendService],
      healthCheck: {
        // 認証不要のエンドポイントでヘルスチェック
        path: '/api/reports/public',
        interval: cdk.Duration.seconds(30),
        healthyThresholdCount: 2,
        unhealthyThresholdCount: 5,
      },
    });

    // フロントエンド ECS サービス
    const frontendService = new ecs.FargateService(this, 'FrontendService', {
      cluster,
      taskDefinition: frontendTaskDef,
      desiredCount: 1,
      securityGroups: [frontendSg],
      assignPublicIp: true,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
    });

    const frontendTg = new elbv2.ApplicationTargetGroup(this, 'FrontendTg', {
      vpc,
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      targets: [frontendService],
      healthCheck: {
        path: '/',
        interval: cdk.Duration.seconds(30),
        healthyThresholdCount: 2,
      },
    });

    // ALB リスナー（デフォルト → フロントエンド、/api/* → バックエンド）
    const listener = alb.addListener('Listener', {
      port: 80,
      defaultTargetGroups: [frontendTg],
    });

    listener.addTargetGroups('BackendRule', {
      priority: 10,
      conditions: [elbv2.ListenerCondition.pathPatterns(['/api/*'])],
      targetGroups: [backendTg],
    });

    // ALB の URL を出力
    new cdk.CfnOutput(this, 'AlbUrl', {
      value: `http://${alb.loadBalancerDnsName}`,
      description: 'アプリケーションのURL',
    });
  }
}
