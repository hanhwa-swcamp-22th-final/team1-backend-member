package com.conk.member.command.infrastructure.service;

/*
 * 비밀번호 관련 기능만 담당하는 서비스다.
 *
 * learned code 기준으로 보면
 * - MemberCommandService가 로그인/초대 같은 업무 흐름을 담당하고
 * - PasswordService는 비밀번호 인코딩/비교 같은 보조 기능만 담당한다.
 */

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String generateTemporaryPassword() {
        return "Tmp!" + UUID.randomUUID().toString().substring(0, 8);
    }
}
