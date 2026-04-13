package com.conk.member.command.service;

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCommandServiceTest {

    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock AccountRepository accountRepository;
    @Mock TenantRepository tenantRepository;

    @InjectMocks AuthService authService;

    private Account account;
    private RefreshToken storedToken;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId("ROLE-001");
        role.setRoleName(RoleName.MASTER_ADMIN);

        account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(role);
        account.setEmail("test@example.com");
        account.setAccountName("테스트유저");
        account.setAccountStatus(AccountStatus.ACTIVE);

        storedToken = RefreshToken.builder()
                .accountId("ACC-001")
                .token("valid-refresh-token")
                .expiryDate(new Date(System.currentTimeMillis() + 3600000))
                .build();
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰 발급 성공")
    void refreshToken_validToken_success() {
        willDoNothing().given(jwtTokenProvider).validateRefreshToken("valid-refresh-token");
        given(jwtTokenProvider.getAccountIdFromJWT("valid-refresh-token")).willReturn("ACC-001");
        given(refreshTokenRepository.findById("ACC-001")).willReturn(Optional.of(storedToken));
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(jwtTokenProvider.createToken(account)).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(account)).willReturn("new-refresh-token");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(604800000L);

        LoginResponse response = authService.refreshToken("valid-refresh-token");

        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("저장된 토큰과 불일치하면 예외 발생")
    void refreshToken_mismatchToken_throwsException() {
        storedToken = RefreshToken.builder()
                .accountId("ACC-001")
                .token("different-token")
                .expiryDate(new Date(System.currentTimeMillis() + 3600000))
                .build();

        willDoNothing().given(jwtTokenProvider).validateRefreshToken("valid-refresh-token");
        given(jwtTokenProvider.getAccountIdFromJWT("valid-refresh-token")).willReturn("ACC-001");
        given(refreshTokenRepository.findById("ACC-001")).willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshToken("valid-refresh-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰이면 예외 발생")
    void refreshToken_expiredToken_throwsException() {
        storedToken = RefreshToken.builder()
                .accountId("ACC-001")
                .token("valid-refresh-token")
                .expiryDate(new Date(System.currentTimeMillis() - 1000))
                .build();

        willDoNothing().given(jwtTokenProvider).validateRefreshToken("valid-refresh-token");
        given(jwtTokenProvider.getAccountIdFromJWT("valid-refresh-token")).willReturn("ACC-001");
        given(refreshTokenRepository.findById("ACC-001")).willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshToken("valid-refresh-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("DB에 저장된 토큰이 없으면 예외 발생")
    void refreshToken_noStoredToken_throwsException() {
        willDoNothing().given(jwtTokenProvider).validateRefreshToken("valid-refresh-token");
        given(jwtTokenProvider.getAccountIdFromJWT("valid-refresh-token")).willReturn("ACC-001");
        given(refreshTokenRepository.findById("ACC-001")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("valid-refresh-token"))
                .isInstanceOf(BadCredentialsException.class);
    }
}
