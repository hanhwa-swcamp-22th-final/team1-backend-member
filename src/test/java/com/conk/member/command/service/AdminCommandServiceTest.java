package com.conk.member.command.service;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.application.service.AdminService;
import com.conk.member.command.application.service.BusinessCodeGenerator;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantLogRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class AdminCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock InvitationRepository invitationRepository;
    @Mock RoleRepository roleRepository;
    @Mock TenantRepository tenantRepository;
    @Mock TenantLogRepository tenantLogRepository;
    @Mock MemberTokenRepository memberTokenRepository;
    @Mock PasswordService passwordService;
    @Mock TokenService tokenService;
    @Mock MailService mailService;
    @Mock BusinessCodeGenerator businessCodeGenerator;

    @InjectMocks AdminService adminService;

    private CreateAdminUserRequest request;
    private Role masterAdminRole;

    @BeforeEach
    void setUp() {
        request = new CreateAdminUserRequest();
        request.setTenantId("TENANT-001");
        request.setName("초기 관리자");
        request.setEmail("master@example.com");
        request.setRole("MASTER_ADMIN");

        masterAdminRole = new Role();
        masterAdminRole.setRoleId("ROLE-002");
        masterAdminRole.setRoleName(RoleName.MASTER_ADMIN);
    }

    @Test
    @DisplayName("최초 총괄 관리자 생성 시 비밀번호 재설정 메일을 발송한다")
    void createAdminUser_sendsPasswordResetMail() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트 업체");

        given(accountRepository.existsByEmail("master@example.com")).willReturn(false);
        given(roleRepository.findByRoleName(RoleName.MASTER_ADMIN)).willReturn(Optional.of(masterAdminRole));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("Temp@1234")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));
        willDoNothing().given(mailService).sendPasswordResetMail(any(), any(), any(), any(), any());

        CreateAdminUserResponse response = adminService.createAdminUser(request);

        assertThat(response.getEmail()).isEqualTo("master@example.com");
        assertThat(response.getRole()).isEqualTo(RoleName.MASTER_ADMIN.name());
        then(mailService).should().sendPasswordResetMail(
                eq("master@example.com"),
                eq("초기 관리자"),
                eq(RoleName.MASTER_ADMIN.name()),
                eq("테스트 업체"),
                eq("Temp@1234")
        );
    }
}
