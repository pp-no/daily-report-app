package com.dailyreport.backend.domain.repository;

import com.dailyreport.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * ユーザーリポジトリ
 *
 * メソッド名がそのままSQLの WHERE 条件になる。
 * findByEmail → WHERE email = ?
 * findByWorkStartTimeAndNotificationEnabled → WHERE work_start_time = ? AND notification_enabled = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ログイン・JWT検証など、メールアドレスでユーザーを特定するために多用する。
     * Optional で返すことで、存在しない場合の NullPointerException を防ぐ。
     */
    Optional<User> findByEmail(String email);

    /**
     * 通知スケジューラーが使用するメソッド。
     * 「業務開始時刻が指定時刻 かつ 通知ON」のユーザーを全件取得する。
     * 毎分実行されるスケジューラーが「現在時刻 + 30分」を渡して対象ユーザーを絞り込む。
     */
    List<User> findByWorkStartTimeAndNotificationEnabled(LocalTime workStartTime, boolean notificationEnabled);
}
