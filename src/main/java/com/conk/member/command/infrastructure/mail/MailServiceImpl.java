package com.conk.member.command.infrastructure.mail;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailServiceImpl(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendInviteMail(String to,
                               String name,
                               String role,
                               String companyName,
                               String temporaryPassword) {

        String subject = createInviteSubject();
        String htmlContent = createInviteHtml(to, name, role, companyName, temporaryPassword);
        String textContent = createInviteText(to, name, role, companyName, temporaryPassword);
        sendMime(to, subject, textContent, htmlContent);
    }

    @Override
    public void sendPasswordResetMail(String to,
                                      String name,
                                      String role,
                                      String companyName,
                                      String temporaryPassword) {

        String subject = createResetSubject();
        String htmlContent = createResetHtml(to, name, role, companyName, temporaryPassword);
        String textContent = createResetText(to, name, role, companyName, temporaryPassword);
        sendMime(to, subject, textContent, htmlContent);
    }

    @Override
    public void sendSetupLink(String to,
                              String name,
                              String companyName,
                              String rawToken) {

        String subject = createSetupSubject();
        String htmlContent = createSetupHtml(name, companyName, rawToken);
        String textContent = createSetupText(name, companyName, rawToken);
        sendMime(to, subject, textContent, htmlContent);
    }

    // ─── sendInviteMail builders ───────────────────────────────────────────────

    private String createInviteSubject() {
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");
        return "[" + serviceName + "] 계정 초대 안내";
    }

    private String createInviteText(String email, String name, String role, String companyName, String temporaryPassword) {
        String loginUrl = buildInviteLoginUrl(email);
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");

        return """
                안녕하세요, %s님.

                %s 계정이 생성되어 초대 메일을 보냅니다.

                소속 회사: %s
                권한: %s
                임시 비밀번호: %s

                아래 URL에서 로그인 후 반드시 비밀번호를 변경해주세요.
                로그인 URL: %s

                감사합니다.
                """.formatted(
                nullToEmpty(name),
                serviceName,
                nullToEmpty(companyName),
                nullToEmpty(role),
                nullToEmpty(temporaryPassword),
                loginUrl
        );
    }

    private String createInviteHtml(String email, String name, String role, String companyName, String temporaryPassword) {
        String safeName = escapeHtml(name);
        String safeRole = escapeHtml(role);
        String safeCompanyName = escapeHtml(companyName);
        String safeTemporaryPassword = escapeHtml(temporaryPassword);
        String safeLoginUrl = escapeHtml(buildInviteLoginUrl(email));
        String safeServiceName = escapeHtml(defaultIfBlank(mailProperties.getServiceName(), "CONK"));

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>계정 초대 메일</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f3f4f6; font-family:Arial, 'Malgun Gothic', sans-serif; color:#111827;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f3f4f6; padding:24px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="640" cellspacing="0" cellpadding="0" border="0"
                                       style="width:640px; max-width:640px; background-color:#ffffff; border:1px solid #e5e7eb; border-radius:16px; overflow:hidden;">
                                    <tr>
                                        <td style="background:#111111; padding:24px 28px; border-bottom:1px solid rgba(255,212,140,0.25);">
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                <tr>
                                                    <td width="52" valign="middle">
                                                        <div style="width:42px; height:42px; line-height:42px; text-align:center; background:#f59e0b; color:#111827; font-size:18px; font-weight:700; border-radius:10px;">CK</div>
                                                    </td>
                                                    <td valign="middle">
                                                        <div style="font-size:28px; font-weight:700; letter-spacing:4px; color:#ffffff; line-height:1; margin-bottom:4px;">CONK</div>
                                                        <div style="font-size:11px; color:#ffffff; opacity:0.78; letter-spacing:1.3px; text-transform:uppercase;">Fulfillment Platform</div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px 28px;">
                                            <div style="font-size:26px; font-weight:700; color:#111827; margin-bottom:12px;">계정 초대 안내</div>
                                            <div style="font-size:15px; line-height:1.8; color:#475569; margin-bottom:24px;">
                                                안녕하세요, <strong style="color:#111827;">%s</strong>님.<br>
                                                <strong style="color:#111827;">%s</strong> 계정이 생성되어 초대 메일을 보냅니다.
                                            </div>
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                                   style="background:#f8fafc; border:1px solid #e5e7eb; border-radius:14px; margin-bottom:24px;">
                                                <tr>
                                                    <td style="padding:20px;">
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">소속 회사</div>
                                                        <div style="font-size:15px; color:#111827; font-weight:700; margin-bottom:14px;">%s</div>
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">권한</div>
                                                        <div style="display:inline-block; padding:6px 10px; background:#fff7e6; border:1px solid #f7c96b; border-radius:999px; font-size:12px; font-weight:700; color:#b45309; margin-bottom:14px;">%s</div>
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">임시 비밀번호</div>
                                                        <div style="font-size:15px; color:#111827; font-weight:700; background:#ffffff; border:1px solid #cbd5e1; border-radius:10px; padding:12px 14px;">%s</div>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="font-size:14px; line-height:1.8; color:#475569; margin-bottom:20px;">아래 버튼을 눌러 로그인한 뒤, 반드시 비밀번호를 변경해주세요.</div>
                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin-bottom:24px;">
                                                <tr>
                                                    <td align="center" bgcolor="#f59e0b" style="border-radius:10px;">
                                                        <a href="%s" style="display:inline-block; padding:14px 24px; color:#111827; font-size:14px; font-weight:700; text-decoration:none;">로그인 바로가기</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="padding:16px; background:#fff7ed; border:1px solid #fed7aa; border-radius:12px; font-size:13px; line-height:1.8; color:#9a3412;">
                                                <strong>안내</strong><br>
                                                • 본 메일은 계정 초대를 위해 발송되었습니다.<br>
                                                • 임시 비밀번호는 최초 로그인 후 변경해주세요.<br>
                                                • 본인이 요청하지 않은 메일이라면 관리자에게 문의해주세요.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:18px 24px; background:#0d0d0d; color:#ffffff; font-size:12px; line-height:1.7;">
                                            <div style="font-size:18px; font-weight:700; letter-spacing:3px; margin-bottom:6px;">CONK</div>
                                            <div>© 2026 CONK Fulfillment Platform. All rights reserved.</div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                safeName, safeServiceName, safeCompanyName, safeRole, safeTemporaryPassword, safeLoginUrl
        );
    }

    // ─── sendPasswordResetMail builders ───────────────────────────────────────

    private String createResetSubject() {
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");
        return "[" + serviceName + "] 임시 비밀번호 안내";
    }

    private String createResetText(String email, String name, String role, String companyName, String temporaryPassword) {
        String loginUrl = buildInviteLoginUrl(email);
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");

        return """
                안녕하세요, %s님.

                %s 계정의 비밀번호가 초기화되었습니다.

                소속 회사: %s
                권한: %s
                임시 비밀번호: %s

                아래 URL에서 로그인 후 반드시 비밀번호를 변경해주세요.
                로그인 URL: %s

                감사합니다.
                """.formatted(
                nullToEmpty(name),
                serviceName,
                nullToEmpty(companyName),
                nullToEmpty(role),
                nullToEmpty(temporaryPassword),
                loginUrl
        );
    }

    private String createResetHtml(String email, String name, String role, String companyName, String temporaryPassword) {
        String safeName = escapeHtml(name);
        String safeRole = escapeHtml(role);
        String safeCompanyName = escapeHtml(companyName);
        String safeTemporaryPassword = escapeHtml(temporaryPassword);
        String safeLoginUrl = escapeHtml(buildInviteLoginUrl(email));
        String safeServiceName = escapeHtml(defaultIfBlank(mailProperties.getServiceName(), "CONK"));

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>임시 비밀번호 안내</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f3f4f6; font-family:Arial, 'Malgun Gothic', sans-serif; color:#111827;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f3f4f6; padding:24px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="640" cellspacing="0" cellpadding="0" border="0"
                                       style="width:640px; max-width:640px; background-color:#ffffff; border:1px solid #e5e7eb; border-radius:16px; overflow:hidden;">
                                    <tr>
                                        <td style="background:#111111; padding:24px 28px; border-bottom:1px solid rgba(255,212,140,0.25);">
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                <tr>
                                                    <td width="52" valign="middle">
                                                        <div style="width:42px; height:42px; line-height:42px; text-align:center; background:#f59e0b; color:#111827; font-size:18px; font-weight:700; border-radius:10px;">CK</div>
                                                    </td>
                                                    <td valign="middle">
                                                        <div style="font-size:28px; font-weight:700; letter-spacing:4px; color:#ffffff; line-height:1; margin-bottom:4px;">CONK</div>
                                                        <div style="font-size:11px; color:#ffffff; opacity:0.78; letter-spacing:1.3px; text-transform:uppercase;">Fulfillment Platform</div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px 28px;">
                                            <div style="font-size:26px; font-weight:700; color:#111827; margin-bottom:12px;">비밀번호 초기화 안내</div>
                                            <div style="font-size:15px; line-height:1.8; color:#475569; margin-bottom:24px;">
                                                안녕하세요, <strong style="color:#111827;">%s</strong>님.<br>
                                                <strong style="color:#111827;">%s</strong> 계정의 비밀번호가 초기화되었습니다.
                                            </div>
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0"
                                                   style="background:#f8fafc; border:1px solid #e5e7eb; border-radius:14px; margin-bottom:24px;">
                                                <tr>
                                                    <td style="padding:20px;">
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">소속 회사</div>
                                                        <div style="font-size:15px; color:#111827; font-weight:700; margin-bottom:14px;">%s</div>
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">권한</div>
                                                        <div style="display:inline-block; padding:6px 10px; background:#fff7e6; border:1px solid #f7c96b; border-radius:999px; font-size:12px; font-weight:700; color:#b45309; margin-bottom:14px;">%s</div>
                                                        <div style="font-size:12px; color:#64748b; font-weight:700; text-transform:uppercase; margin-bottom:6px;">임시 비밀번호</div>
                                                        <div style="font-size:15px; color:#111827; font-weight:700; background:#ffffff; border:1px solid #cbd5e1; border-radius:10px; padding:12px 14px;">%s</div>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="font-size:14px; line-height:1.8; color:#475569; margin-bottom:20px;">아래 버튼을 눌러 로그인한 뒤, 반드시 비밀번호를 변경해주세요.</div>
                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin-bottom:24px;">
                                                <tr>
                                                    <td align="center" bgcolor="#f59e0b" style="border-radius:10px;">
                                                        <a href="%s" style="display:inline-block; padding:14px 24px; color:#111827; font-size:14px; font-weight:700; text-decoration:none;">로그인 바로가기</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="padding:16px; background:#fff7ed; border:1px solid #fed7aa; border-radius:12px; font-size:13px; line-height:1.8; color:#9a3412;">
                                                <strong>안내</strong><br>
                                                • 본인이 요청하지 않은 비밀번호 초기화라면 즉시 관리자에게 문의해주세요.<br>
                                                • 임시 비밀번호는 최초 로그인 후 변경해주세요.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:18px 24px; background:#0d0d0d; color:#ffffff; font-size:12px; line-height:1.7;">
                                            <div style="font-size:18px; font-weight:700; letter-spacing:3px; margin-bottom:6px;">CONK</div>
                                            <div>© 2026 CONK Fulfillment Platform. All rights reserved.</div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                safeName, safeServiceName, safeCompanyName, safeRole, safeTemporaryPassword, safeLoginUrl
        );
    }

    // ─── sendSetupLink builders ────────────────────────────────────────────────

    private String createSetupSubject() {
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");
        return "[" + serviceName + "] 최초 비밀번호 설정 안내";
    }

    private String buildInviteLoginUrl(String email) {
        String baseUrl = defaultIfBlank(mailProperties.getLoginUrl(), "#");
        String separator = baseUrl.contains("?") ? "&" : "?";
        String encodedEmail = URLEncoder.encode(defaultIfBlank(email, ""), StandardCharsets.UTF_8);
        return baseUrl + separator + "forceLogin=1&loginHint=" + encodedEmail;
    }

    private String createSetupText(String name, String companyName, String rawToken) {
        String setupUrl = buildSetupUrl(rawToken);
        String serviceName = defaultIfBlank(mailProperties.getServiceName(), "CONK");

        return """
                안녕하세요, %s님.

                %s 서비스에 오신 것을 환영합니다.
                소속 회사: %s

                아래 URL에서 최초 비밀번호를 설정해주세요.
                설정 링크: %s

                이 링크는 7일간 유효하며 1회만 사용할 수 있습니다.

                감사합니다.
                """.formatted(
                nullToEmpty(name),
                serviceName,
                nullToEmpty(companyName),
                setupUrl
        );
    }

    private String createSetupHtml(String name, String companyName, String rawToken) {
        String safeName = escapeHtml(name);
        String safeCompanyName = escapeHtml(companyName);
        String safeSetupUrl = escapeHtml(buildSetupUrl(rawToken));
        String safeServiceName = escapeHtml(defaultIfBlank(mailProperties.getServiceName(), "CONK"));

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>최초 비밀번호 설정</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f3f4f6; font-family:Arial, 'Malgun Gothic', sans-serif; color:#111827;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f3f4f6; padding:24px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="640" cellspacing="0" cellpadding="0" border="0"
                                       style="width:640px; max-width:640px; background-color:#ffffff; border:1px solid #e5e7eb; border-radius:16px; overflow:hidden;">
                                    <tr>
                                        <td style="background:#111111; padding:24px 28px; border-bottom:1px solid rgba(255,212,140,0.25);">
                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                                <tr>
                                                    <td width="52" valign="middle">
                                                        <div style="width:42px; height:42px; line-height:42px; text-align:center; background:#f59e0b; color:#111827; font-size:18px; font-weight:700; border-radius:10px;">CK</div>
                                                    </td>
                                                    <td valign="middle">
                                                        <div style="font-size:28px; font-weight:700; letter-spacing:4px; color:#ffffff; line-height:1; margin-bottom:4px;">CONK</div>
                                                        <div style="font-size:11px; color:#ffffff; opacity:0.78; letter-spacing:1.3px; text-transform:uppercase;">Fulfillment Platform</div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px 28px;">
                                            <div style="font-size:26px; font-weight:700; color:#111827; margin-bottom:12px;">최초 비밀번호 설정 안내</div>
                                            <div style="font-size:15px; line-height:1.8; color:#475569; margin-bottom:24px;">
                                                안녕하세요, <strong style="color:#111827;">%s</strong>님.<br>
                                                <strong style="color:#111827;">%s</strong> 서비스에 오신 것을 환영합니다.<br>
                                                소속 회사로 <strong style="color:#111827;">%s</strong>에 등록되었습니다.
                                            </div>
                                            <div style="font-size:14px; line-height:1.8; color:#475569; margin-bottom:20px;">아래 버튼을 눌러 최초 비밀번호를 설정해주세요.</div>
                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin-bottom:24px;">
                                                <tr>
                                                    <td align="center" bgcolor="#f59e0b" style="border-radius:10px;">
                                                        <a href="%s" style="display:inline-block; padding:14px 24px; color:#111827; font-size:14px; font-weight:700; text-decoration:none;">비밀번호 설정하기</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="padding:16px; background:#fff7ed; border:1px solid #fed7aa; border-radius:12px; font-size:13px; line-height:1.8; color:#9a3412;">
                                                <strong>안내</strong><br>
                                                • 이 링크는 발송 후 7일간 유효합니다.<br>
                                                • 링크는 1회만 사용할 수 있습니다.<br>
                                                • 본인이 요청하지 않은 메일이라면 무시하셔도 됩니다.
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:18px 24px; background:#0d0d0d; color:#ffffff; font-size:12px; line-height:1.7;">
                                            <div style="font-size:18px; font-weight:700; letter-spacing:3px; margin-bottom:6px;">CONK</div>
                                            <div>© 2026 CONK Fulfillment Platform. All rights reserved.</div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                safeName, safeServiceName, safeCompanyName, safeSetupUrl
        );
    }

    // ─── shared helpers ────────────────────────────────────────────────────────

    private void sendMime(String to, String subject, String textContent, String htmlContent) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, htmlContent);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException | UnsupportedEncodingException e) {
            throw new MemberException(ErrorCode.INTERNAL_ERROR, "메일 발송에 실패했습니다.");
        }
    }

    private String buildSetupUrl(String rawToken) {
        return mailProperties.getSetupBaseUrl() + "?token=" + rawToken;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
