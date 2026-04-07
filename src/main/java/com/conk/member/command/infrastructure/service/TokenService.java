package com.conk.member.command.infrastructure.service;

/*
 * 학습용 토큰 보조 서비스다.
 *
 * JWT 발급 자체는 JwtTokenProvider가 담당하고,
 * 이 클래스는 최초 비밀번호 설정용 토큰처럼 별도로 저장해야 하는 토큰의
 * 원문 생성과 해시 생성을 담당한다.
 */

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {

    public String createSetupToken() {
        return UUID.randomUUID().toString();
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("토큰 해시 생성 중 오류가 발생했습니다.", exception);
        }
    }
}
