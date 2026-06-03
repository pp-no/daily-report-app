package com.dailyreport.backend.domain.repository;

import com.dailyreport.backend.domain.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 日報リポジトリ
 *
 * 【JpaRepository<DailyReport, Long>】
 * Spring Data JPA が提供する汎用リポジトリ。
 * findById / save / delete などの基本的なCRUDメソッドが最初から使える。
 * Long は主キーの型。
 *
 * 【メソッド名によるSQLの自動生成（命名規則）】
 * findBy〇〇And△△OrderBy□□Desc のようなメソッド名から
 * Spring Data JPA が自動でSQLを生成する。自分でSQLを書かなくていい。
 */
@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    /**
     * ユーザーIDと日付が一致する日報を1件取得。
     * 同じ日に日報は1件しか作れない制約を確認するためにも使う。
     * Optional = 見つからない場合も例外を投げずに空を返す。
     */
    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);

    /** 自分の日報を日付の新しい順で取得。 */
    List<DailyReport> findByUserIdOrderByReportDateDesc(Long userId);

    /** 公開フラグが true の日報を全ユーザー分取得。 */
    List<DailyReport> findByIsPublicTrueOrderByReportDateDesc();

    /**
     * 【@Query】命名規則では表現しにくい複雑な条件のSQLをJPQL（Java用のSQL的なもの）で書く。
     * ここでは「指定日付の日報 かつ 通知ONのユーザー」を一度に取得している。
     * r.user.notificationEnabled のように関連エンティティのフィールドも参照できる。
     */
    @Query("SELECT r FROM DailyReport r WHERE r.reportDate = :date AND r.user.notificationEnabled = true")
    List<DailyReport> findReportsForNotification(@Param("date") LocalDate date);
}
