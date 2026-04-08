package com.conk.member.command.application.service;

/*
 * 로그인 서비스 단위 테스트.
 * 구버전(AuthService/LoginRequest/LoginResponse) 참조를 실제 MemberCommandService 기준으로 재작성.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.*;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.jwt.JwtTokenProvider;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

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

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("이메일로 로그인할 수 있다")
    void login_with_email_success() {
        Account account = activeAccount("ACC-001", "admin@conk.com", RoleName.MASTER_ADMIN);

        when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));
        when(passwordService.matches("raw-password", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createToken("ACC-001", "MASTER_ADMIN")).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(any(), any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("admin@conk.com");
        request.setPassword("raw-password");

        MemberResponses.LoginResponse response = memberCommandService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("admin@conk.com");
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRole()).isEqualTo("MASTER_ADMIN");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("작업자 코드로 로그인할 수 있다")
    void login_with_worker_code_success() {
        Account account = activeAccount("ACC-002", null, RoleName.WAREHOUSE_WORKER);
        account.setWorkerCode("WORKER-001");

        when(accountRepository.findByEmail("WORKER-001")).thenReturn(Optional.empty());
        when(accountRepository.findByWorkerCode("WORKER-001")).thenReturn(Optional.of(account));
        when(passwordService.matches("worker-pass", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createToken("ACC-002", "WAREHOUSE_WORKER")).thenReturn("worker-token");
        when(jwtTokenProvider.createRefreshToken(any(), any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("WORKER-001");
        request.setPassword("worker-pass");

        MemberResponses.LoginResponse response = memberCommandService.login(request);

        assertThat(response.getToken()).isEqualTo("worker-token");
        assertThat(response.getRole()).isEqualTo("WAREHOUSE_WORKER");
    }

    @Test
    @DisplayName("이메일/비밀번호가 틀리면 로그인에 실패한다")
    void login_fail_when_wrong_password() {
        Account account = activeAccount("ACC-001", "admin@conk.com", RoleName.MASTER_ADMIN);

        when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));
        when(passwordService.matches("wrong-password", "encoded-password")).thenReturn(false);

        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("admin@conk.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> memberCommandService.login(request))
            .isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 로그인하면 실패한다")
    void login_fail_when_account_not_found() {
        when(accountRepository.findByEmail("nobody@conk.com")).thenReturn(Optional.empty());
        when(accountRepository.findByWorkerCode("nobody@conk.com")).thenReturn(Optional.empty());

        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("nobody@conk.com");
        request.setPassword("any-pass");

        assertThatThrownBy(() -> memberCommandService.login(request))
            .isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("비활성화된 계정은 로그인에 실패한다")
    void login_fail_when_account_inactive() {
        Account account = activeAccount("ACC-001", "inactive@conk.com", RoleName.MASTER_ADMIN);
        account.setAccountStatus(AccountStatus.INACTIVE);

        when(accountRepository.findByEmail("inactive@conk.com")).thenReturn(Optional.of(account));
        when(passwordService.matches("raw-password", "encoded-password")).thenReturn(true);

        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("inactive@conk.com");
        request.setPassword("raw-password");

        assertThatThrownBy(() -> memberCommandService.login(request))
            .isInstanceOf(MemberException.class);
    }

    private Account activeAccount(String accountId, String email, RoleName roleName) {
        Role role = new Role();
        role.setRoleId("ROLE-" + roleName.name());
        role.setRoleName(roleName);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole(role);
        account.setEmail(email);
        account.setAccountName("테스트 사용자");
        account.setPasswordHash("encoded-password");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        return account;
    }
}
