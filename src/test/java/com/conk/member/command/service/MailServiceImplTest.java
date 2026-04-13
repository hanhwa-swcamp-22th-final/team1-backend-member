package com.conk.member.command.service;

import com.conk.member.command.infrastructure.mail.MailProperties;
import com.conk.member.command.infrastructure.mail.MailServiceImpl;
import com.conk.member.common.exception.MemberException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailServiceImplTest {

    @Mock JavaMailSender javaMailSender;
    @Mock MailProperties mailProperties;

    @InjectMocks MailServiceImpl mailServiceImpl;

    // MimeMessageHelper가 실제 Session 기반 MimeMessage를 필요로 함
    private MimeMessage realMimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailServiceImpl, "fromEmail", "no-reply@conk.com");

        given(mailProperties.getFromName()).willReturn("CONK");
        given(mailProperties.getLoginUrl()).willReturn("http://localhost:3000/login");
        given(mailProperties.getServiceName()).willReturn("CONK");
        given(mailProperties.getSetupBaseUrl()).willReturn("http://localhost:3000/member/setup-password");

        realMimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        given(javaMailSender.createMimeMessage()).willReturn(realMimeMessage);
    }

    @Nested
    @DisplayName("sendInviteMail")
    class SendInviteMail {

        @Test
        @DisplayName("정상 발송 - JavaMailSender.send() 호출됨")
        void sendInviteMail_success() {
            mailServiceImpl.sendInviteMail(
                    "manager@example.com", "홍길동", "WAREHOUSE_MANAGER", "테스트업체", "Temp@1234");

            then(javaMailSender).should().createMimeMessage();
            then(javaMailSender).should().send(realMimeMessage);
        }

        @Test
        @DisplayName("name/role/companyName이 null이어도 예외 없이 발송")
        void sendInviteMail_withNullFields_success() {
            assertThatCode(() ->
                    mailServiceImpl.sendInviteMail("a@b.com", null, null, null, "pwd"))
                    .doesNotThrowAnyException();

            then(javaMailSender).should().send(realMimeMessage);
        }

        @Test
        @DisplayName("serviceName 미설정 시 기본값 CONK 사용")
        void sendInviteMail_blankServiceName_usesDefault() {
            given(mailProperties.getServiceName()).willReturn(null);

            assertThatCode(() ->
                    mailServiceImpl.sendInviteMail("a@b.com", "홍길동", "SELLER", "업체", "pwd"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("메일 발송 실패 시 MemberException 발생")
        void sendInviteMail_mailException_throwsMemberException() {
            willThrow(new MailSendException("SMTP 오류")).given(javaMailSender).send(realMimeMessage);

            assertThatThrownBy(() ->
                    mailServiceImpl.sendInviteMail("a@b.com", "홍길동", "SELLER", "업체", "pwd"))
                    .isInstanceOf(MemberException.class)
                    .hasMessageContaining("메일 발송에 실패했습니다.");
        }
    }

    @Nested
    @DisplayName("sendPasswordResetMail")
    class SendPasswordResetMail {

        @Test
        @DisplayName("정상 발송 - JavaMailSender.send() 호출됨")
        void sendPasswordResetMail_success() {
            mailServiceImpl.sendPasswordResetMail(
                    "admin@example.com", "김관리", "MASTER_ADMIN", "테스트업체", "Reset@5678");

            then(javaMailSender).should().send(realMimeMessage);
        }

        @Test
        @DisplayName("name/role/companyName이 null이어도 예외 없이 발송")
        void sendPasswordResetMail_withNullFields_success() {
            assertThatCode(() ->
                    mailServiceImpl.sendPasswordResetMail("a@b.com", null, null, null, "pwd"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("메일 발송 실패 시 MemberException 발생")
        void sendPasswordResetMail_mailException_throwsMemberException() {
            willThrow(new MailSendException("SMTP 오류")).given(javaMailSender).send(realMimeMessage);

            assertThatThrownBy(() ->
                    mailServiceImpl.sendPasswordResetMail("a@b.com", "홍길동", "MASTER_ADMIN", "업체", "pwd"))
                    .isInstanceOf(MemberException.class);
        }
    }

    @Nested
    @DisplayName("sendSetupLink")
    class SendSetupLink {

        @Test
        @DisplayName("정상 발송 - JavaMailSender.send() 호출됨")
        void sendSetupLink_success() {
            mailServiceImpl.sendSetupLink(
                    "admin@example.com", "김대표", "테스트업체", "raw-token-abc");

            then(javaMailSender).should().send(realMimeMessage);
        }

        @Test
        @DisplayName("setupBaseUrl 미설정 시 기본 URL 사용")
        void sendSetupLink_blankSetupBaseUrl_usesDefault() {
            given(mailProperties.getSetupBaseUrl()).willReturn(null);

            assertThatCode(() ->
                    mailServiceImpl.sendSetupLink("a@b.com", "홍길동", "업체", "token"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("메일 발송 실패 시 MemberException 발생")
        void sendSetupLink_mailException_throwsMemberException() {
            willThrow(new MailSendException("SMTP 오류")).given(javaMailSender).send(realMimeMessage);

            assertThatThrownBy(() ->
                    mailServiceImpl.sendSetupLink("a@b.com", "홍길동", "업체", "token"))
                    .isInstanceOf(MemberException.class);
        }
    }

    @Nested
    @DisplayName("HTML 이스케이프")
    class HtmlEscape {

        @Test
        @DisplayName("XSS 시도 문자가 포함된 입력도 예외 없이 처리")
        void sendInviteMail_xssInput_doesNotThrow() {
            assertThatCode(() ->
                    mailServiceImpl.sendInviteMail(
                            "a@b.com",
                            "<script>alert('xss')</script>",
                            "SELLER",
                            "<b>악성업체</b>",
                            "pwd&123"))
                    .doesNotThrowAnyException();

            then(javaMailSender).should().send(realMimeMessage);
        }
    }

    @Nested
    @DisplayName("메일 발송 횟수")
    class SendCount {

        @Test
        @DisplayName("sendInviteMail 1회 호출 시 send() 정확히 1번 호출")
        void sendInviteMail_calledOnce() {
            mailServiceImpl.sendInviteMail("a@b.com", "홍", "SELLER", "업체", "pwd");

            then(javaMailSender).should(times(1)).send(realMimeMessage);
        }

        @Test
        @DisplayName("sendPasswordResetMail 1회 호출 시 send() 정확히 1번 호출")
        void sendPasswordResetMail_calledOnce() {
            mailServiceImpl.sendPasswordResetMail("a@b.com", "홍", "MASTER_ADMIN", "업체", "pwd");

            then(javaMailSender).should(times(1)).send(realMimeMessage);
        }

        @Test
        @DisplayName("sendSetupLink 1회 호출 시 send() 정확히 1번 호출")
        void sendSetupLink_calledOnce() {
            mailServiceImpl.sendSetupLink("a@b.com", "홍", "업체", "token");

            then(javaMailSender).should(times(1)).send(realMimeMessage);
        }
    }
}
