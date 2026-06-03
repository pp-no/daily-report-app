package com.dailyreport.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日報エンティティ（DBのdaily_reportsテーブルに対応）
 *
 * 【@Entity】このクラスがDBのテーブルにマッピングされることを宣言する。
 * 【@Table(name = "daily_reports")】対応するテーブル名を明示する。
 *   省略するとクラス名がテーブル名になる。
 * 【@Getter / @Setter】全フィールドのgetterとsetterをLombokが自動生成する。
 */
@Entity
@Table(name = "daily_reports")
@Getter
@Setter
public class DailyReport {

    /** 【@Id】主キー。【@GeneratedValue(IDENTITY)】DBのAUTO INCREMENT で自動採番。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 【@ManyToOne】多対1の関係（日報:ユーザー = 多:1）。
     * 【fetch = FetchType.LAZY】このフィールドへのアクセス時にだけSQLを発行する遅延読み込み。
     *   EAGER（即時読み込み）にするとユーザー情報が不要な場合でもJOINが走り非効率。
     * 【@JoinColumn(name = "user_id")】DBの外部キーカラム名を指定する。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, length = 200)
    private String title;

    /** 【columnDefinition = "TEXT"】VARCHAR(255)の上限を超える長文テキスト用のカラム型。 */
    @Column(name = "today_tasks", nullable = false, columnDefinition = "TEXT")
    private String todayTasks;

    @Column(name = "tomorrow_tasks", nullable = false, columnDefinition = "TEXT")
    private String tomorrowTasks;

    // 任意入力のため nullable（null 許可）
    @Column(columnDefinition = "TEXT")
    private String impression;

    // メール通知で使用する。任意入力のため nullable
    @Column(columnDefinition = "TEXT")
    private String summary;

    // デフォルトは非公開
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    /**
     * 【@CreationTimestamp】INSERT時にHibernateが自動で現在日時をセットする。
     * 【updatable = false】一度セットされたら更新されない。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 【@UpdateTimestamp】UPDATE時にHibernateが自動で現在日時を更新する。 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
