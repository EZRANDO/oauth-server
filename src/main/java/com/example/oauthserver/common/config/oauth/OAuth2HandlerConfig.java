package com.example.oauthserver.common.config.oauth;

import com.example.oauthserver.common.config.jwt.TokenProvider;
import com.example.oauthserver.common.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.example.oauthserver.common.repository.RefreshTokenRepository;
import com.example.oauthserver.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2HandlerConfig {

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(
            OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository,
            TokenProvider tokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            UserService userService
    ) {
        return new OAuth2SuccessHandler(
                authorizationRequestRepository,
                tokenProvider,
                refreshTokenRepository,
                userService
        );
    }
}