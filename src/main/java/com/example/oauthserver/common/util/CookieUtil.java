package com.example.oauthserver.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class CookieUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //요청값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
    //HttpServletResposne: 서버가 클라이언트에게 응답을 보낼 때 사용하는 java servlet API인터페이스
    //1. new Cookie로 쿠키 객체 생성
    //2. setPath로 어디에서 이 쿠키가 유효한지 설정
    //3. setMaxAge로 만료시간 설정
    //4. addCookie로 응답헤더에 추가.
    public static void addCookie(HttpServletResponse res, String name, String value, int maxAgeSec, boolean https) {

        Cookie c = new Cookie(name, value);
        c.setPath("/");
        c.setMaxAge(maxAgeSec);
        c.setHttpOnly(true);

        if (https) {
            c.setSecure(true);
            res.addHeader("Set-Cookie",
                    String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            name, value, maxAgeSec));
        } else {
            res.addHeader("Set-Cookie",
                    String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                            name, value, maxAgeSec));
        }
        res.addCookie(c);
    }

    public static boolean isHttps(HttpServletRequest req) {
        String proto = req.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(proto) || req.isSecure();
    }

    //쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return; //쿠키가 비어있으면 아무것도 하지 않음.
        }

        //값을 모두 비움
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    //쿠키에 저장된 Base64문자열을 디코딩 -> JSON문자열 복원
    //Jackson으로 JSON문자열을 객체로 역직렬화
//    public static <T> T deserializeJsonCookie(Cookie cookie, Class<T> cls) {
//        try {
//            String json = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
//            return objectMapper.readValue(json, cls);
//        } catch (IOException e) {
//            throw new RuntimeException("쿠키 역직렬화 실패", e);
//        }
//    }
//
//    //객체를 Jackson을 사용해 JSON문자열로 변환
//    public static String serializeJson(Object obj) {
//        try {
//            String json = objectMapper.writeValueAsString(obj);
//            return Base64.getUrlEncoder().encodeToString(json.getBytes());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("쿠키 직렬화 실패", e);
//        }
//    }
    // 직렬화: 객체 → byte[] → Base64 문자열
    public static String serializeJson(Object obj) {
        byte[] bytes = SerializationUtils.serialize(obj);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    // 역직렬화: Base64 문자열 → byte[] → 객체
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String encoded, Class<T> cls) {
        byte[] bytes = Base64.getUrlDecoder().decode(encoded);
        return (T) SerializationUtils.deserialize(bytes);
    }

    public static <T> T deserializeJsonCookie(Cookie cookie, Class<T> cls) {
        if (cookie == null) return null; // ← 가드
        return deserialize(cookie.getValue(), cls);
    }

}