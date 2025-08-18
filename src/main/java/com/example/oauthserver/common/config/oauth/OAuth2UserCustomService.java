package com.example.oauthserver.common.config.oauth;

import com.example.oauthserver.common.repository.UserRepository;
import com.example.oauthserver.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

//소셜 로그인 이후, 로그인한 사용자의 정보를 받아와 서비스의 user로 저장 or 업데이트
//소셜에서 받은 사용자는 DB에없을 수 있어서 첫 로그인시엔 저장이 필요.
//이미 존재하면 이름or프로필 정보가 갱신 가능성이 있어 업뎃.
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "kakao", "google" 등
        Map<String, Object> attrs = oauth2User.getAttributes();

        // ----- Kakao 전용 파싱 -----
        String providerId = String.valueOf(attrs.get("id")); // 카카오는 최상위 id
        Map<String, Object> account = (Map<String, Object>) attrs.get("kakao_account");
        String email = account != null ? (String) account.get("email") : null; // 선택 동의라 null 가능
        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : null;

        saveOrUpdateOAuthUser(provider.toUpperCase(), providerId, email, nickname);

        // 그대로 돌려줘도 되지만, 필요 시 DefaultOAuth2User로 감싸 반환 가능
        return oauth2User;
    }

    private void saveOrUpdateOAuthUser(String provider, String providerId, String email, String nickname) {
        userRepository.findByProviderAndProviderId(provider, providerId)
                .map(u -> {
                    if (nickname != null) u.setNickname(nickname);
                    if (email != null)    u.setEmail(email); // 이메일 동의 시에만 업데이트
                    return u;
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setProvider(provider);
                    u.setProviderId(providerId);
                    u.setEmail(email);
                    u.setNickname(nickname);
                    return userRepository.save(u);
                });
    }
}