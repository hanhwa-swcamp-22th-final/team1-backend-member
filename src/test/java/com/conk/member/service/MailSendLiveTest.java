package com.conk.member.service;

import com.conk.member.command.infrastructure.mail.MailProperties;
import com.conk.member.command.infrastructure.mail.MailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 실제 Gmail SMTP로 메일을 발송하는 라이브 테스트.
 * DB 연결 불필요 — application-local.yaml 설정을 직접 주입.
 */
class MailSendLiveTest {

    private static final String TARGET = "@naver.com";

    private MailServiceImpl buildMailService() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername("mail0175823@gmail.com");
        sender.setPassword("fhgb xwqa mjrz flru");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        MailProperties mailProperties = new MailProperties();
        mailProperties.setFromName("CONK Fulfillment Platform");
        mailProperties.setLoginUrl("http://localhost:3000/login");
        mailProperties.setServiceName("CONK");
        mailProperties.setSetupBaseUrl("http://localhost:3000/member/setup-password");

        MailServiceImpl service = new MailServiceImpl(sender, mailProperties);
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "fromEmail", "mail0175823@gmail.com");
        return service;
    }

    @Test
    void 초대_메일_발송() {
        buildMailService().sendInviteMail(
                TARGET, "ㅋㅋ", "WAREHOUSE_MANAGER", "테스트업체", "Temp@1234");
        System.out.println("[초대 메일] 발송 완료 → " + TARGET);
    }

    @Test
    void 비밀번호_재설정_메일_발송() {
        buildMailService().sendPasswordResetMail(
                TARGET, "김수닐", "MASTER_ADMIN", "테스트업체", "Reset@5678");
        System.out.println("[비밀번호 재설정 메일] 발송 완료 → " + TARGET);
    }

    @Test
    void 최초_비밀번호_설정_링크_발송() {
        buildMailService().sendSetupLink(
                TARGET, "김수닐", "테스트업체", "sample-token-xyz-123");
        System.out.println("[설정 링크 메일] 발송 완료 → " + TARGET);
    }
}
