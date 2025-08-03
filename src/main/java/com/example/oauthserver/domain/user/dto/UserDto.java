package com.example.oauthserver.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.example.oauthserver.domain.user.entity.User;

@Getter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String nickname;

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}