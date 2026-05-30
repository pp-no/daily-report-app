package com.dailyreport.backend.domain.repository;

import com.dailyreport.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByWorkStartTimeAndNotificationEnabled(LocalTime workStartTime, boolean notificationEnabled);
}
