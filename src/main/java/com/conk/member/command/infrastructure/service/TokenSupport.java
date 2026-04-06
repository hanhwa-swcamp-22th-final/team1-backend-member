package com.conk.member.command.infrastructure.service;

/*
 * 액세스 토큰/설정 토큰 발급과 해시 생성을 담당하는 지원 서비스다.
 */

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Component
public class TokenSupport {
    public String createAccessToken(String accountId, String roleName) {
        return "access-" + accountId + "-" + roleName + "-" + UUID.randomUUID();
    }
    public String createSetupToken() { return "setup-" + UUID.randomUUID(); }
    public String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
