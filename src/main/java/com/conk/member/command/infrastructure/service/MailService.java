package com.conk.member.command.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender javaMailSender;
    private final String fromAddress;
    private final String setupBaseUrl;

    public MailService(ObjectProvider<JavaMailSender> javaMailSenderProvider,
                       @Value("${spring.mail.username:no-reply@conk.local}") String fromAddress,
                       @Value("${app.mail.setup-base-url:http://localhost:3000/member/setup-password}") String setupBaseUrl) {
        this.javaMailSender = javaMailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
        this.setupBaseUrl = setupBaseUrl;
    }

    public void sendSetupLink(String email, String rawToken) {
        String subject = "CONK 최초 비밀번호 설정";
        String body = "최초 비밀번호 설정 링크입니다.\n" +
                setupBaseUrl + "?token=" + rawToken + "\n" +
                "이 링크는 1회만 사용할 수 있습니다.";
        send(email, subject, body, "setup link", rawToken);
    }

    public void sendTemporaryPassword(String email, String temporaryPassword) {
        String subject = "CONK 임시 비밀번호 안내";
        String body = "임시 비밀번호가 발급되었습니다.\n" +
                "임시 비밀번호: " + temporaryPassword + "\n" +
                "로그인 후 최초 비밀번호를 변경해 주세요.";
        send(email, subject, body, "temporary password", temporaryPassword);
    }

    private void send(String email, String subject, String body, String logType, String rawValue) {
        if (javaMailSender == null) {
            log.info("[MAIL-FALLBACK] {} to={} payload={}", logType, email, rawValue);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
        } catch (Exception exception) {
            log.warn("메일 전송에 실패하여 로그로 대체합니다. to={} message={}", email, exception.getMessage());
            log.info("[MAIL-FALLBACK] {} to={} payload={}", logType, email, rawValue);
        }
    }
}
