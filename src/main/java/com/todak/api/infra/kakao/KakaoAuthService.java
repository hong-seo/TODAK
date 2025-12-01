package com.todak.api.infra.kakao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException; // 추가된 import
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${kakao.client-id}")
    private String clientId;

    // application.yml에 redirect-uri 추가 필요 (프론트엔드 주소와 일치해야 함)
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 1. 인가 코드로 카카오 Access Token 받아오기
     */
    public String getAccessToken(String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri); // 카카오에 등록한 주소와 100% 일치해야 함
        params.add("code", authCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // [수정 2] try-catch로 감싸서 에러 상세 확인
        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    request,
                    KakaoTokenResponse.class
            );

            return response.getBody().getAccessToken();

        } catch (HttpClientErrorException e) {
            // 🚨 여기서 에러 내용을 콘솔에 출력합니다.
            String errorBody = e.getResponseBodyAsString();
            System.out.println("==================================================");
            System.out.println("🚨 [KakaoAuthService] 카카오 토큰 발급 실패!");
            System.out.println("👉 상태 코드: " + e.getStatusCode());
            System.out.println("👉 응답 내용: " + errorBody);
            System.out.println("==================================================");

            // 상세 내용을 포함하여 예외를 다시 던짐
            throw new RuntimeException("카카오 로그인 실패 (토큰 요청): " + errorBody);
        }
    }

    /**
     * 2. Access Token으로 유저 정보 가져오기 (기존 메서드 유지)
     */
    public Long getKakaoUserId(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    entity,
                    KakaoUserInfoResponse.class
            );

            return response.getBody().getId();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오 유저 정보 조회 실패: " + e.getMessage());
        }
    }

    @Getter
    static class KakaoUserInfoResponse {
        private Long id;
    }

    @Getter
    static class KakaoTokenResponse {
        private String access_token;
        private String refresh_token;

        public String getAccessToken() { return access_token; }
    }
}