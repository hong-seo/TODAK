package com.todak.api.auth.service;

import com.todak.api.auth.dto.response.TokenResponse;
import com.todak.api.auth.jwt.JwtTokenProvider;
import com.todak.api.infra.kakao.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthService kakaoAuthService;

    // 병원(관리자) 로그인
    public TokenResponse hospitalLogin(String loginId, String password) {
        if ("admin".equals(loginId) && "1234".equals(password)) {
            String accessToken = jwtTokenProvider.createToken(1L, "ADMIN");
            return new TokenResponse(accessToken, "refresh_token_dummy");
        }
        throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    /**
     * 카카오 로그인 (앱에서 받은 AccessToken으로 로그인 처리)
     */
    public TokenResponse kakaoLogin(String kakaoAccessToken) {
        Long kakaoUserId = kakaoAuthService.getKakaoUserId(kakaoAccessToken);
        // 3. 우리 서비스 전용 JWT 토큰 발급
        String accessToken = jwtTokenProvider.createToken(kakaoUserId, "USER");

        return new TokenResponse(accessToken, "refresh_token_dummy");
    }
}