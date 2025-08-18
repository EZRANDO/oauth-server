package com.example.oauthserver.common.config.oauth;

import com.example.oauthserver.common.config.jwt.TokenProvider;
import com.example.oauthserver.common.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.example.oauthserver.common.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authReqCookieRepo;

    //보안 필터체인을 아예 타지 않게 하는 설정 로그인 인증없이 가능함.
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers("/css/**", "/js/**", "/img/**", "/h2-console/**");
//    }

    //보안 규칙 정의. 어떤 방식으로 인증 인가할지.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //로그인한 사용자의 요청에 대해 JWT를 파싱해 인증 처리를 해주는 필터
                //"/api/token": 로그인/토큰 재발급용 API → 누구나 접근 가능
                //"/api/**": 나머지 API → JWT가 있어야만 접근 가능
                //기타 요청: 모두 허용 (anyRequest().permitAll())
                .authorizeHttpRequests(auth -> auth
                        // 공통 정적 리소스(부트 기본 경로)
                        .requestMatchers(org.springframework.boot.autoconfigure.security.servlet.PathRequest
                                .toStaticResources().atCommonLocations()).permitAll()
                        // 커스텀 정적 경로
                        .requestMatchers("/img/**", "/css/**", "/js/**", "/webjars/**",
                                "/my-assets/**", "/other-folder/**").permitAll()
                        // OAuth 시작/콜백 및 공개 엔드포인트
                        .requestMatchers("/oauth2/**", "/login/**", "/auth/**", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // 공개 API
                        .requestMatchers("/api/auth/firebase-login", "/api/token").permitAll()
                        .requestMatchers("/",  "/login/oauth2/**", "/login/success", "/static/**").permitAll()
                        // 보호 API
                        .requestMatchers("/api/**").authenticated()

                        // 나머지 (운영에선 authenticated 권장)
                        .anyRequest().authenticated()
                )

                //로그인한 사용자의 요청에 대ㅐ해 jwt를 파싱해서 인증처리를 해주는 필터. 시큐리티 체인보다 앞에 배치됨.
                .addFilterBefore(
                        new TokenAuthenticationFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                //오어스 로그인 요청하면 쿠키에 요청정보 저장하고 소셜 로그인 정보 받아올 서비스 지정, 성공시엔 토믄 발금
                //saveAuthrozationReuqest를 호출시켜야 쿠키가 생성됨. ㅁ
                .oauth2Login(o -> o
                        .loginPage("/login")
                        .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(authReqCookieRepo))
                        .redirectionEndpoint(r -> r.baseUri("/auth/login/*")) // 콜백 베이스
                        .userInfoEndpoint(u -> u.userService(oAuth2UserCustomService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .build();
    }

//    @Bean
//    public OAuth2SuccessHandler oAuth2SuccessHandler(UserService userService) {
//        return new OAuth2SuccessHandler(
//                authorizationRequestRepository,
//                tokenProvider,
//                refreshTokenRepository,// 정확한 메서드 호출
//                userService
//        );
//    }

//    @Bean
//    public OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository() {
//        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
//    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
