package com.example.oauthserver.common.config.oauth;

import com.example.oauthserver.common.repository.UserRepository;
import com.example.oauthserver.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

//소셜 로그인 이후, 로그인한 사용자의 정보를 받아와 서비스의 user로 저장 or 업데이트
//소셜에서 받은 사용자는 DB에없을 수 있어서 첫 로그인시엔 저장이 필요.
//이미 존재하면 이름or프로필 정보가 갱신 가능성이 있어 업뎃.
@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User user = super.loadUser(userRequest);

        saveOrUpdate(user); //저장or업뎃

        return user;
    }

    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes(); //사용자 정보 가져옴.

        String email = (String) attributes.get("email");

        String name = (String) attributes.get("name");

         User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name)) //존재시  업뎃
                .orElse(User.builder() //없으면 새로 생성
                        .email(email)
                        .nickname(name)
                        .build());

         return userRepository.save(user);
    }
}
