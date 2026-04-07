package com.conk.member.command.application.service;

/*
 * 핵심 command 로직을 단위 테스트로 검증한다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.domain.aggregate.*;
import com.conk.member.command.domain.enums.*;
import com.conk.member.command.domain.repository.*;
import com.conk.member.command.infrastructure.service.*;
import com.conk.member.common.jwt.JwtTokenProvider;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private InvitationRepository invitationRepository;
    @Mock private MemberTokenRepository memberTokenRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private RolePermissionHistoryRepository rolePermissionHistoryRepository;
    @Mock private PasswordService passwordService;
    @Mock private TokenService tokenService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private MailService mailService;
    @Mock private WarehouseService warehouseService;
    @InjectMocks private MemberCommandService memberCommandService;

    @Test
    @DisplayName("이메일로 로그인할 수 있다")
    void login_success() {
        Role role = new Role(); role.setRoleId("ROLE-001"); role.setRoleName(RoleName.MASTER_ADMIN);
        Account account = new Account();
        account.setAccountId("ACC-001"); account.setRole(role); account.setEmail("master@conk.com"); account.setPasswordHash("encoded"); account.setAccountStatus(AccountStatus.ACTIVE); account.setTenantId("TENANT-001");
        Tenant tenant = new Tenant(); tenant.setTenantId("TENANT-001"); tenant.setTenantName("FASTSHIP");
        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest(); request.setEmailOrWorkerCode("master@conk.com"); request.setPassword("raw");
        when(accountRepository.findByEmail("master@conk.com")).thenReturn(Optional.of(account));
        when(passwordService.matches("raw", "encoded")).thenReturn(true);
        when(jwtTokenProvider.createToken(any(), any())).thenReturn("token");
        when(jwtTokenProvider.createRefreshToken(any(), any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);
        when(tenantRepository.findById("TENANT-001")).thenReturn(Optional.of(tenant));
        assertThat(memberCommandService.login(request).getToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("이미 사용된 설정 토큰은 다시 사용할 수 없다")
    void setup_password_already_used_fail() {
        MemberToken token = new MemberToken(); token.setIsUsed(Boolean.TRUE); token.setExpiresAt(LocalDateTime.now().plusDays(1));
        MemberRequests.SetupPasswordRequest request = new MemberRequests.SetupPasswordRequest(); request.setSetupToken("raw"); request.setNewPassword("new");
                when(memberTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        assertThatThrownBy(() -> memberCommandService.setupPassword(request)).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("마지막 활성 총괄관리자는 비활성화할 수 없다")
    void last_active_master_admin_fail() {
        Role role = new Role(); role.setRoleName(RoleName.MASTER_ADMIN);
        Account account = new Account(); account.setAccountId("ACC-001"); account.setTenantId("TENANT-001"); account.setRole(role); account.setAccountStatus(AccountStatus.ACTIVE);
        when(accountRepository.findById("ACC-001")).thenReturn(Optional.of(account));
        when(accountRepository.countByTenantIdAndRoleNameAndAccountStatus("TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);
        assertThatThrownBy(() -> memberCommandService.deactivate("ACC-001")).isInstanceOf(MemberException.class);
    }
}
