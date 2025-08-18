package com.example.oauthserver.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Setter
@NoArgsConstructor
@Getter
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "password")
    private String password;           // 소셜만 쓰면 null 허용

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private String provider;           // "KAKAO", "GOOGLE" 등

    @Column(name = "provider_id", nullable = false)
    private String providerId;         // 소셜 고유 ID (문자열 권장)

    @Column
    private String email;              // 선택 동의 → null 허용

    @Builder
    public User(String provider, String providerId, String email, String password, String nickname) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ★ 기본 규칙: ROLE_ 접두어
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return provider + ":" + providerId;
    }

    @Override
    public String getPassword() { return password; }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    public static User ofSocial(String provider, String providerId, String email, String nickname) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .build();
    }
}