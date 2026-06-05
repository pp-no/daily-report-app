package com.dailyreport.backend.service;

import com.dailyreport.backend.api.dto.ChangePasswordRequest;
import com.dailyreport.backend.api.dto.UserRequest;
import com.dailyreport.backend.api.dto.UserResponse;
import com.dailyreport.backend.domain.entity.User;
import com.dailyreport.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザーサービス
 *
 * プロフィールの取得と更新を担当する。
 * 認証（ログイン・パスワード照合）は AuthService が担当し、ここでは扱わない。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** プロフィール情報を取得する。パスワードは UserResponse に含めないため安全に返せる。 */
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return UserResponse.from(findByEmail(email));
    }

    /**
     * プロフィールを更新する。
     * @Transactional が有効なため、フィールドをセットするだけで save() 呼び出し後にDBへ反映される。
     */
    public UserResponse updateProfile(UserRequest request, String email) {
        User user = findByEmail(email);

        // メールアドレスを変更する場合、他のユーザーと重複していないか確認
        if (!user.getEmail().equals(request.email())) {
            userRepository.findByEmail(request.email()).ifPresent(other -> {
                throw new IllegalArgumentException("このメールアドレスは既に使用されています");
            });
        }

        user.setName(request.name());
        user.setEmail(request.email());
        // workStartTime が null の場合は既存の値を保持する（フロントが送ってこない場合も考慮）
        if (request.workStartTime() != null) {
            user.setWorkStartTime(request.workStartTime());
        }
        user.setNotificationEnabled(request.notificationEnabled());

        return UserResponse.from(userRepository.save(user));
    }

    /** パスワードを変更する。現在のパスワードを BCrypt で照合してから新しいパスワードに更新する。 */
    public void changePassword(ChangePasswordRequest request, String email) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("新しいパスワードと確認用パスワードが一致しません");
        }

        User user = findByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: " + email));
    }
}
