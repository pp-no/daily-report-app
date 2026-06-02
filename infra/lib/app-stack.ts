import * as cdk from 'aws-cdk-lib/core';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';
import { BaseStack } from './base-stack';

interface AppStackProps extends cdk.StackProps {
  baseStack: BaseStack;
}

export class AppStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: AppStackProps) {
    super(scope, id, props);

    const { vpc, backendRepo, frontendRepo, db, dbSecret, appSecret, dbSg } = props.baseStack;

    // ===== セキュリティグループ =====

    // ALB（インターネットからのHTTPのみ許可）
    const albSg = new ec2.SecurityGroup(this, 'AlbSg', {
      vpc,
      description: 'ALB security group',
    });
    albSg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'HTTP from internet');

    // バックエンド ECS（ALBからの8080のみ許可）
    const backendSg = new ec2.SecurityGroup(this, 'BackendSg', {
      vpc,
      description: 'Backend ECS security group',
    });
    backendSg.addIngressRule(albSg, ec2.Port.tcp(8080), 'From ALB');

    // フロントエンド ECS（ALBからの80のみ許可）
    const frontendSg = new ec2.SecurityGroup(this, 'FrontendSg', {
      vpc,
      description: 'Frontend ECS security group',
    });
    frontendSg.addIngressRule(albSg, ec2.Port.tcp(80), 'From ALB');

    // RDS へのアクセスをバックエンド ECS からのみ許可
    dbSg.addIngressRule(backendSg, ec2.Port.tcp(5432), 'From backend ECS');

    // ===== ECS クラスター =====
    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc,
      clusterName: 'dailyreport',
    });

    // ===== バックエンド タスク定義 =====
    const backendTaskDef = new ecs.FargateTaskDefinition(this, 'BackendTaskDef', {
      cpu: 512,
      memoryLimitMiB: 1024,
    });

    backendTaskDef.addContainer('backend', {
      image: ecs.ContainerImage.fromEcrRepository(backendRepo, 'latest'),
      portMappings: [{ containerPort: 8080 }],
      environment: {
        // DB ホストは RDS エンドポイントを環境変数で渡す
        SPRING_DATASOURCE_URL: `jdbc:postgresql://${db.dbInstanceEndpointAddress}:5432/dailyreport`,
        // SES SMTP エンドポイント（リージョンに合わせて自動設定）
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

    // ===== フロントエンド タスク定義 =====
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

    // ===== ALB =====
    const alb = new elbv2.ApplicationLoadBalancer(this, 'Alb', {
      vpc,
      internetFacing: true,
      securityGroup: albSg,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
    });

    // ===== ECS サービス + ターゲットグループ =====

    // バックエンド（パブリックサブネットで公開IPを付与しECRイメージを取得）
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

    // フロントエンド
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

    // ===== ALB リスナー =====
    // デフォルト → フロントエンド、/api/* → バックエンド
    const listener = alb.addListener('Listener', {
      port: 80,
      defaultTargetGroups: [frontendTg],
    });

    listener.addTargetGroups('BackendRule', {
      priority: 10,
      conditions: [elbv2.ListenerCondition.pathPatterns(['/api/*'])],
      targetGroups: [backendTg],
    });

    // ===== 出力 =====
    new cdk.CfnOutput(this, 'AlbUrl', {
      value: `http://${alb.loadBalancerDnsName}`,
      description: 'アプリケーションのURL',
    });
  }
}
