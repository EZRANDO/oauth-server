# Firebase Google Login with Spring Boot 
**Firebase Google 로그인**을 사용하여 발급받은 **ID 토큰**을 Spring Boot 서버에서 검증하는 예제입니다.

---

## 📂 구성 파일
- **`FirebaseInitializer.java`**  
  - Firebase Admin SDK 초기화 (서비스 계정 키 사용)
- **`FirebaseAuthService.java`**  
  - ID 토큰 검증 로직
- **`FirebaseAuthController.java`**  
  - `/api/auth/firebase-login` POST 엔드포인트
- **`firebase-adminsdk.json`**  
  - Firebase 서비스 계정 비공개 키 (**Gitingnore**)
- **`google.html`**  
  - 테스트용 프론트엔드 페이지 (Google 로그인 버튼)

---

## 프로젝트 구조
```
/project-root
├─ src/main/java/.../firebase
│  ├─ FirebaseInitializer.java        # Admin SDK 초기화
│  ├─ FirebaseAuthService.java        # ID 토큰 검증 서비스
│  └─ FirebaseAuthController.java     # /api/auth/firebase-login 엔드포인트
├─ src/main/resources/firebase
│  └─ firebase-adminsdk.json          # 서비스 계정 키(비공개, git ignore)
└─ google.html                         # 테스트용 프론트(버튼 1개)
```
