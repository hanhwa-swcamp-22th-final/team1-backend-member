package com.conk.member.service;

import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.SetupPasswordCommandService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.enums.TokenType;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SetupPasswordCommandServiceTest {

    @Mock TokenService tokenService;
    @Mock MemberTokenRepository memberTokenRepository;
    @Mock AccountRepository accountRepository;
    @Mock PasswordService passwordService;
    @Mock TenantRepository tenantRepository;

    @InjectMocks SetupPasswordCommandService setupPasswordCommandService;

    private MemberToken memberToken;
    private Account account;
    private Role masterAdminRole;

    @BeforeEach
    void setUp() {
        masterAdminRole = new Role();
        masterAdminRole.setRoleId("ROLE-001");
        masterAdminRole.setRoleName(RoleName.MASTER_ADMIN);

        account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(masterAdminRole);
        account.setTenantId("TENANT-001");
        account.setAccountStatus(AccountStatus.TEMP_PASSWORD);
        account.setPasswordHash("$2a$old");

        memberToken = new MemberToken();
        memberToken.setTokenId("TOKEN-001");
        memberToken.setAccountId("ACC-001");
        memberToken.setTokenHash("hashed-token");
        memberToken.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        memberToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        memberToken.setIsUsed(false);
    }

    @Test
    @DisplayName("비밀번호 설정 성공 - 일반 계정")
    void setupPassword_nonMasterAdmin_success() {
        Role workerRole = new Role();
        workerRole.setRoleName(RoleName.WAREHOUSE_WORKER);
        account.setRole(workerRole);
        account.setTenantId(null);

        SetupPasswordRequest request = new SetupPasswordRequest();
        request.setSetupToken("raw-token");
        request.setNewPassword("newPassword123!");

        given(tokenService.hash("raw-token")).willReturn("hashed-token");
        given(memberTokenRepository.findByTokenHash("hashed-token")).willReturn(Optional.of(memberToken));
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(passwordService.encode("newPassword123!")).willReturn("$2a$new");

        SetupPasswordResponse response = setupPasswordCommandService.setupPassword(request);

        assertThat(response.getAccountId()).isEqualTo("ACC-001");
        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE.name());
        then(memberTokenRepository).should().save(memberToken);
        assertThat(memberToken.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("비밀번호 설정 성공 - MASTER_ADMIN이면 테넌트 활성화")
    void setupPassword_masterAdmin_activatesTenant() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트업체");
        tenant.setStatus(TenantStatus.SETTING);

        SetupPasswordRequest request = new SetupPasswordRequest();
        request.setSetupToken("raw-token");
        request.setNewPassword("newPassword123!");

        given(tokenService.hash("raw-token")).willReturn("hashed-token");
        given(memberTokenRepository.findByTokenHash("hashed-token")).willReturn(Optional.of(memberToken));
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(passwordService.encode("newPassword123!")).willReturn("$2a$new");
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));

        SetupPasswordResponse response = setupPasswordCommandService.setupPassword(request);

        assertThat(response.getTenantStatus()).isEqualTo(TenantStatus.ACTIVE.name());
        assertThat(response.getActivatedAt()).isNotNull();
        then(tenantRepository).should().save(tenant);
    }

    @Test
    @DisplayName("이미 사용된 토큰으로 요청하면 예외 발생")
    void setupPassword_alreadyUsedToken_throwsException() {
        memberToken.setIsUsed(true);

        SetupPasswordRequest request = new SetupPasswordRequest();
        request.setSetupToken("raw-token");
        request.setNewPassword("newPassword123!");

        given(tokenService.hash("raw-token")).willReturn("hashed-token");
        given(memberTokenRepository.findByTokenHash("hashed-token")).willReturn(Optional.of(memberToken));

        assertThatThrownBy(() -> setupPasswordCommandService.setupPassword(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.TOKEN_ALREADY_USED));
    }

    @Test
    @DisplayName("만료된 토큰으로 요청하면 예외 발생")
    void setupPassword_expiredToken_throwsException() {
        memberToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        memberToken.setIsUsed(false);

        SetupPasswordRequest request = new SetupPasswordRequest();
        request.setSetupToken("raw-token");
        request.setNewPassword("newPassword123!");

        given(tokenService.hash("raw-token")).willReturn("hashed-token");
        given(memberTokenRepository.findByTokenHash("hashed-token")).willReturn(Optional.of(memberToken));

        assertThatThrownBy(() -> setupPasswordCommandService.setupPassword(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외 발생")
    void setupPassword_invalidToken_throwsException() {
        SetupPasswordRequest request = new SetupPasswordRequest();
        request.setSetupToken("invalid-token");
        request.setNewPassword("newPassword123!");

        given(tokenService.hash("invalid-token")).willReturn("unknown-hash");
        given(memberTokenRepository.findByTokenHash("unknown-hash")).willReturn(Optional.empty());

        assertThatThrownBy(() -> setupPasswordCommandService.setupPassword(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}
