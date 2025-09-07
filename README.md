# JWT 기반 인증/인가 서버

**JWT 인증/인가 서버**로 Access Token과 Refresh Token을 활용하여 사용자 인증을 처리합니다.  
Refresh Token은 DB에 저장하고, 쿠키(HttpOnly, Secure 옵션)에 심어 브라우저에서 안전하게 유지할 수 있도록 구성되어 있습니다.  
OAuth2 kakao와 연동하여 로그인 성공 시 토큰을 발급하고, 보호된 API 접근 시 JWT를 통한 인증 절차를 거칩니다.

---

## 📂 프로젝트 구조
```
com.example.auth
├─ common
│ ├─ jwt
│ │ ├─ JwtProperties.java # JWT 설정값(시크릿키, 만료시간 등) 관리
│ │ └─ TokenProvider.java # JWT 생성, 검증, 인증 복원 로직
│ ├─ security
│ │ ├─ SecurityConfig.java # Spring Security 보안 설정
│ │ ├─ TokenAuthenticationFilter.java # 요청에서 JWT 파싱·검증 필터
│ │ └─ oauth
│ │ ├─ KakaoOAuth2UserService.java # Kakao OAuth2 사용자 정보 처리
│ │ ├─ OAuth2SuccessHandler.java # 소셜 로그인 성공 후 토큰 발급/쿠키 저장
│ │ └─ OAuth2FailureHandler.java # 소셜 로그인 실패 처리
│ └─ util
│ └─ CookieUtil.java # 쿠키 생성, 삭제, 직렬화/역직렬화 유틸
├─ domain
│ ├─ user
│ │ ├─ User.java # 사용자 엔티티
│ │ ├─ Role.java # 권한(Enum)
│ │ └─ UserRepository.java # 사용자 JPA 리포지토리
│ └─ token
│ ├─ RefreshToken.java # Refresh Token 엔티티
│ └─ RefreshTokenRepository.java # Refresh Token JPA 리포지토리
├─ service
│ └─ UserService.java # 사용자 비즈니스 로직
└─ web
└─ AuthController.java # 인증 관련 API 컨트롤러
```
---
## 📌 인증 플로우
1. 사용자가 OAuth2 소셜 로그인 요청
2. OAuth2 인증 서버(Kakao)에서 사용자 정보 제공
3. `OAuth2SuccessHandler`에서 Access Token, Refresh Token 발급
4. Refresh Token DB 저장 + 쿠키 저장
5. 보호 API 요청 시 `TokenAuthenticationFilter`에서 토큰 검증
6. 토큰 유효하면 SecurityContext에 인증 정보 저장 후 API 접근 허용
