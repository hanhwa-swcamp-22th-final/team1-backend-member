package com.conk.member.command.infrastructure.service;

/*
 * 메일 전송 역할을 담당하는 서비스다.
 * 현재는 실제 메일 서버 대신 로그를 남기는 학습용 구현이다.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public void sendSetupLink(String email, String rawToken) {
        log.info("[MAIL] setup link to={} token={}", email, rawToken);
    }

    public void sendTemporaryPassword(String email, String temporaryPassword) {
        log.info("[MAIL] temp password to={} password={}", email, temporaryPassword);
    }
}
