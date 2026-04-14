package com.conk.member.command.service;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
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
class LoginCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock PasswordService passwordService;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock TenantRepository tenantRepository;

    @InjectMocks AuthService authService;

    private Role role;
    private Account account;

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
        account.setPasswordHash("$2a$encoded");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setTenantId("TENANT-001");
    }

    @Test
    @DisplayName("이메일로 로그인 성공")
    void login_withEmail_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트업체");
        tenant.setStatus(TenantStatus.ACTIVE);

        given(accountRepository.findByEmail("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("password123", "$2a$encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(account)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(account)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getName()).isEqualTo("테스트유저");
        assertThat(response.getUser().getOrganization()).isEqualTo("테스트업체");
    }

    @Test
    @DisplayName("작업자 코드로 로그인 성공")
    void login_withWorkerCode_success() {
        account.setEmail(null);
        account.setWorkerCode("WC-001");

        LoginRequest request = new LoginRequest();
        request.setEmail("WC-001");
        request.setPassword("password123");

        given(accountRepository.findByEmail("WC-001")).willReturn(Optional.empty());
        given(accountRepository.findByWorkerCode("WC-001")).willReturn(Optional.of(account));
        given(passwordService.matches("password123", "$2a$encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(account)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(account)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 로그인 실패")
    void login_accountNotFound_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("none@example.com");
        request.setPassword("password123");

        given(accountRepository.findByEmail("none@example.com")).willReturn(Optional.empty());
        given(accountRepository.findByWorkerCode("none@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("비밀번호 불일치로 로그인 실패")
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        given(accountRepository.findByEmail("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("wrong", "$2a$encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("비활성화된 계정으로 로그인 실패")
    void login_inactiveAccount_throwsException() {
        account.setAccountStatus(AccountStatus.INACTIVE);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        given(accountRepository.findByEmail("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("password123", "$2a$encoded")).willReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("임시 비밀번호 상태로 로그인 성공 (TEMP_PASSWORD는 허용)")
    void login_tempPasswordStatus_success() {
        account.setAccountStatus(AccountStatus.TEMP_PASSWORD);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        given(accountRepository.findByEmail("test@example.com")).willReturn(Optional.of(account));
        given(passwordService.matches("password123", "$2a$encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(account)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(account)).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
    }
}
