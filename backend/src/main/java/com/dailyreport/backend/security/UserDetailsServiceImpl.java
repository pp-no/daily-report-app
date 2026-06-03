package com.dailyreport.backend.security;

import com.dailyreport.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security のユーザー情報提供クラス
 *
 * 【役割】Spring Security が「このメールアドレスのユーザーはDBに存在するか？
 * パスワードは何か？」を問い合わせる際に呼ばれるクラス。
 *
 * 【UserDetailsService】Spring Security が定義するインターフェース。
 * このクラスを implements して @Service に登録することで、
 * AuthService の authenticationManager.authenticate() が内部で自動的に呼び出す。
 *
 * 【loadUserByUsername の "Username"】Spring Security の命名だが、
 * このアプリではメールアドレスを識別子として使っている。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * メールアドレスでDBからユーザーを取得し、Spring Security が扱える UserDetails 形式に変換する。
     * 見つからない場合は UsernameNotFoundException をスローする（Spring Security が 401 に変換する）。
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword()) // BCryptハッシュ値
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
    }
}
