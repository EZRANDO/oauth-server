package com.example.oauthserver.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Base64;

public class CookieUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //요청값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
    //HttpServletResposne: 서버가 클라이언트에게 응답을 보낼 때 사용하는 java servlet API인터페이스
    //1. new Cookie로 쿠키 객체 생성
    //2. setPath로 어디에서 이 쿠키가 유효한지 설정
    //3. setMaxAge로 만료시간 설정
    //4. addCookie로 응답헤더에 추가.
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);

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
    public static <T> T deserializeJsonCookie(Cookie cookie, Class<T> cls) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
            return objectMapper.readValue(json, cls);
        } catch (IOException e) {
            throw new RuntimeException("쿠키 역직렬화 실패", e);
        }
    }

    //객체를 Jackson을 사용해 JSON문자열로 변환
    public static String serializeJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("쿠키 직렬화 실패", e);
        }
    }
}