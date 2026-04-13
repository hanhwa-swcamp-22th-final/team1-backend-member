package com.conk.member.command.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.UserService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TenantRepository tenantRepository;
    @Mock PasswordService passwordService;
    @Mock MailService mailService;

    @InjectMocks UserService userService;

    private Account account;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleId("ROLE-001");
        role.setRoleName(RoleName.MASTER_ADMIN);

        account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(role);
        account.setEmail("test@example.com");
        account.setAccountName("테스트유저");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setTenantId("TENANT-001");
    }

    @Test
    @DisplayName("비밀번호 초기화 성공 - 이메일 발송됨")
    void resetPassword_success_sendsMail() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트업체");
        tenant.setStatus(TenantStatus.ACTIVE);

        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("Temp@1234")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));
        willDoNothing().given(mailService).sendPasswordResetMail(any(), any(), any(), any(), any());

        SimpleUserStatusResponse response = userService.resetPassword("ACC-001");

        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.TEMP_PASSWORD.name());
        assertThat(response.getIsTemporaryPassword()).isTrue();
        then(mailService).should().sendPasswordResetMail(
                eq("test@example.com"), eq("테스트유저"), eq(RoleName.MASTER_ADMIN.name()), eq("테스트업체"), eq("Temp@1234"));
    }

    @Test
    @DisplayName("이메일 없는 계정은 메일 발송 안 함")
    void resetPassword_noEmail_doesNotSendMail() {
        account.setEmail(null);

        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("Temp@1234")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        SimpleUserStatusResponse response = userService.resetPassword("ACC-001");

        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.TEMP_PASSWORD.name());
        then(mailService).should(never()).sendPasswordResetMail(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 비밀번호 초기화 시 예외 발생")
    void resetPassword_userNotFound_throwsException() {
        given(accountRepository.findById("ACC-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword("ACC-999"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }
}
