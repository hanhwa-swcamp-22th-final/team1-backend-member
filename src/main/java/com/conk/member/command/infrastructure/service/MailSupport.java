package com.conk.member.command.infrastructure.service;

/*
 * 실제 메일 서버 대신 로그로 대체한 메일 지원 서비스다.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MailSupport {
    private static final Logger log = LoggerFactory.getLogger(MailSupport.class);
    public void sendSetupLink(String email, String rawToken) { log.info("[MAIL] setup link to={} token={}", email, rawToken); }
    public void sendTemporaryPassword(String email, String password) { log.info("[MAIL] temp password to={} password={}", email, password); }
}
