package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.UserRequest;
import com.dailyreport.backend.api.dto.UserResponse;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("テストユーザー");
        testUser.setEmail("test@example.com");
        testUser.setWorkStartTime(LocalTime.of(9, 0));
        testUser.setNotificationEnabled(true);
    }

    // =========================================================
    // getProfile
    // =========================================================

    @Test
    void getProfile_正常系_ユーザー情報が返る() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getProfile("test@example.com");

        assertThat(result.name()).isEqualTo("テストユーザー");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.notificationEnabled()).isTrue();
    }

    @Test
    void getProfile_ユーザーが存在しない場合_例外がスロー() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("notfound@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ユーザーが見つかりません");
    }

    // =========================================================
    // updateProfile
    // =========================================================

    @Test
    void updateProfile_正常系_ユーザー情報が更新される() {
        UserRequest request = new UserRequest("更新後名前", "test@example.com", LocalTime.of(10, 0), false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateProfile(request, "test@example.com");

        assertThat(result.name()).isEqualTo("更新後名前");
        assertThat(result.workStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.notificationEnabled()).isFalse();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_メールアドレスを別の未使用アドレスに変更できる() {
        UserRequest request = new UserRequest("テストユーザー", "new@example.com", LocalTime.of(9, 0), true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        // 新メールアドレスは未使用
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateProfile(request, "test@example.com");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_メールアドレスが他ユーザーと重複する場合_例外がスロー() {
        UserRequest request = new UserRequest("テストユーザー", "existing@example.com", LocalTime.of(9, 0), true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        // 変更先メールアドレスは既に別ユーザーが使用中
        User other = new User();
        other.setEmail("existing@example.com");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateProfile(request, "test@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("既に使用されています");
        verify(userRepository, never()).save(any());
    }
}
