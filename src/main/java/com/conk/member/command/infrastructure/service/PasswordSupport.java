package com.conk.member.command.infrastructure.service;

/*
 * 비밀번호 인코딩/비교/임시비밀번호 생성을 담당하는 지원 서비스다.
 */

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PasswordSupport {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String raw) { return encoder.encode(raw); }
    public boolean matches(String raw, String encoded) { return encoder.matches(raw, encoded); }
    public String generateTemporaryPassword() { return "Tmp!" + UUID.randomUUID().toString().substring(0, 8); }
}
