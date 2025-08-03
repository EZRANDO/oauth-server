package com.example.oauthserver.domain.user.service;

import com.example.oauthserver.domain.user.dto.UserDto;
import com.example.oauthserver.domain.user.entity.User;
import com.example.oauthserver.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // CREATE
    public User createUser(String email, String nickname, String password) {
        User user = User.create(email, nickname, password); // 정적 팩토리
        return userRepository.save(user);
    }

    // READ - 전체 사용자 조회 (Stream + DTO 변환)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::from) // User → UserDto
                .collect(Collectors.toList());
    }

    // READ - ID로 조회
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::from)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));
    }

    // UPDATE
    @Transactional
    public void updateUser(Long id, String nickname, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));
        user.update(nickname, password);
    }

    // DELETE
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("이미 삭제된 사용자입니다.");
        }
        userRepository.deleteById(id);
    }
}