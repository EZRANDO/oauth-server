package com.example.oauthserver.common.config.oauth;

import com.example.oauthserver.common.config.jwt.TokenProvider;
import com.example.oauthserver.common.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.example.oauthserver.common.repository.RefreshTokenRepository;
import com.example.oauthserver.common.util.CookieUtil;
import com.example.oauthserver.domain.RefreshToken;
import com.example.oauthserver.domain.User;
import com.example.oauthserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String REDIRECT_PATH = "/articles";

    private final OAuth2AuthorizationRequestBasedOnCookieRepository oauth2AuthorizationRequestBasedOnCookieRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // ★ 여기: 더 이상 email 의존 금지
        //   provider/providerId로 사용자 찾기
        var attrs = oAuth2User.getAttributes();
        String provider = authentication.getAuthorities().toString(); // 권장: userRequest에서 registrationId로 받기
        String providerId = String.valueOf(attrs.get("id"));          // kakao 최상위 id
        User user = userService.findByProviderAndProviderId("KAKAO", providerId);

        // 토큰 생성
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        String accessToken  = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        // ★ 액세스 토큰도 쿠키로 심기
        boolean https = CookieUtil.isHttps(request);
        CookieUtil.addCookie(response, "ACCESS_TOKEN", accessToken, (int) ACCESS_TOKEN_DURATION.toSeconds(), https);

        clearAuthenticationAttributes(request, response);

        // ★ 토큰 없이 깨끗한 경로로 리다이렉트(permitAll 권장)
        getRedirectStrategy().sendRedirect(request, response, "/login/success");
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);

        boolean https = CookieUtil.isHttps(request);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken, cookieMaxAge, https);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {

        super.clearAuthenticationAttributes(request);

        oauth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequest(request, response);
    }

    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
//현재 OAuth2SuccessHandler 흐름을 보면 리프레시 토큰만 쿠키로 심고, **액세스 토큰은 URL 쿼리 파라미터(?token=...)**로 넘깁니다. 이 구조라면:
//
//서버 측 필터가 Authorization 헤더나 쿠키에서 액세스 토큰을 찾는 경우, 리다이렉트된 /articles?token=... 요청에는 헤더/쿠키가 없어서 인증 실패 → 다시 로그인으로 튕김 → 리디렉션 과다가 발생합니다.
//
//게다가 액세스 토큰을 URL에 노출하면 로그/리퍼러에 남아 보안상 좋지 않습니다.