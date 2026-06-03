package com.dailyreport.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ユーザーエンティティ（DBのusersテーブルに対応）
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 【unique = true】メールアドレスの重複を DB レベルで禁止する。 */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt でハッシュ化されたパスワードを保存する。生パスワードは保存しない。 */
    @Column(nullable = false, length = 255)
    private String password;

    /** メール通知のトリガー時刻。デフォルト 09:00 なら 08:30 に通知が届く。 */
    @Column(name = "work_start_time", nullable = false)
    private LocalTime workStartTime = LocalTime.of(9, 0);

    /** false にするとスケジューラーがこのユーザーをスキップする。 */
    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 【@OneToMany】1対多の関係（ユーザー:日報 = 1:多）。
     * 【mappedBy = "user"】DailyReport.user フィールドが外部キーを管理していることを示す。
     * 【cascade = ALL】ユーザー削除時に関連する日報も全て削除される。
     * 【fetch = LAZY】日報一覧は必要な時だけ取得する（パフォーマンス対策）。
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyReport> dailyReports;
}
