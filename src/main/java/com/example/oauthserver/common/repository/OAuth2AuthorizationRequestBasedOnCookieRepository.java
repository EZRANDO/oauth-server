package com.example.oauthserver.common.repository;

import com.example.oauthserver.common.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class OAuth2AuthorizationRequestBasedOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2AuthorizationRequest";

    private final static int COOKIE_EXPIRE_SECONDS = 60 * 60 * 24 * 7;

    //OAuth인증 정보를 쿠키에 저장하고, 꺼내고, 삭제하는 역할을 수행하는 클래스
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest httpServletRequest) {

        Cookie cookie = WebUtils.getCookie(httpServletRequest, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

        return CookieUtil.deserializeJsonCookie(cookie, OAuth2AuthorizationRequest.class);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {

        return this.loadAuthorizationRequest(request);

    }

    //null이 아니면 객체 직렬화후 쿠키 저장
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        boolean https = CookieUtil.isHttps(request);
        String payload = CookieUtil.serializeJson(authorizationRequest); // 공주님 기존 직렬화 사용 시
        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, payload, COOKIE_EXPIRE_SECONDS, https);
    }

    //saveAuthorizationRequest에서만 호출하기 때문에 private로 선언.
    private void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
