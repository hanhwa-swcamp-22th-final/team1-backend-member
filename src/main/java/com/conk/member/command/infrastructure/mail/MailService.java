package com.conk.member.command.infrastructure.mail;

public interface MailService {

    // MEM-005, MEM-017: 초대·총괄관리자 추가 시 임시 비밀번호 발송
    void sendInviteMail(String to,
                        String name,
                        String role,
                        String companyName,
                        String temporaryPassword);

    // MEM-007: 비밀번호 초기화 시 임시 비밀번호 발송
    void sendPasswordResetMail(String to,
                               String name,
                               String role,
                               String companyName,
                               String temporaryPassword);

    // MEM-014: 업체 최초 등록 시 비밀번호 설정 링크 발송
    void sendSetupLink(String to,
                       String name,
                       String companyName,
                       String rawToken);
}
