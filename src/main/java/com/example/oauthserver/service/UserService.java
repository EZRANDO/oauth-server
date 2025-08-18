package com.example.oauthserver.service;

import com.example.oauthserver.common.repository.UserRepository;
import com.example.oauthserver.domain.User;
import com.example.oauthserver.dto.AddUserRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long save(AddUserRequest dto) {
        // 로컬 계정 정책
        final String provider = "LOCAL";
        final String providerId = dto.getEmail(); // 또는 UUID.randomUUID().toString()

        // 중복 방지(동일 provider+providerId)
        userRepository.findByProviderAndProviderId(provider, providerId)
                .ifPresent(u -> { throw new IllegalArgumentException("이미 가입된 사용자입니다."); });

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = User.builder()
                .provider(provider)               // ★ 필수
                .providerId(providerId)           // ★ 필수
                .email(dto.getEmail())            // 로컬에선 보통 필수
                .password(encoder.encode(dto.getPassword()))
                .build();

        return userRepository.save(user).getId();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByProviderAndProviderId(String kakao, String providerId) {
        return userRepository.findByProviderAndProviderId(kakao, providerId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
}