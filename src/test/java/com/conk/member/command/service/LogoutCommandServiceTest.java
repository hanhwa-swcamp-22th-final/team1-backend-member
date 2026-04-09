package com.conk.member.command.service;

import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
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
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutCommandServiceTest {

    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks AuthService authService;

    private static final String ACCOUNT_ID = "ACC-001";
    private static final String REFRESH_TOKEN = "valid-refresh-token";

    private RefreshToken storedToken;

    @BeforeEach
    void setUp() {
        storedToken = RefreshToken.builder()
                .accountId(ACCOUNT_ID)
                .token(REFRESH_TOKEN)
                .expiryDate(new Date(System.currentTimeMillis() + 60_000))
                .build();
    }

    @Test
    @DisplayName("로그아웃 성공 - refresh token 삭제됨")
    void logout_success() {
        given(jwtTokenProvider.getAccountIdFromJWT(REFRESH_TOKEN)).willReturn(ACCOUNT_ID);
        given(refreshTokenRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(storedToken));

        authService.logout(REFRESH_TOKEN);

        then(refreshTokenRepository).should().deleteById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("유효하지 않은 refresh token - 예외 발생")
    void logout_invalidToken_throwsException() {
        willThrow(new BadCredentialsException("Invalid token"))
                .given(jwtTokenProvider).validateRefreshToken(REFRESH_TOKEN);

        assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                .isInstanceOf(BadCredentialsException.class);

        then(refreshTokenRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("DB에 refresh token 없음 - 예외 발생")
    void logout_tokenNotFound_throwsException() {
        given(jwtTokenProvider.getAccountIdFromJWT(REFRESH_TOKEN)).willReturn(ACCOUNT_ID);
        given(refreshTokenRepository.findById(ACCOUNT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token을 찾을 수 없습니다.");

        then(refreshTokenRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("저장된 token과 요청 token 불일치 - 예외 발생")
    void logout_tokenMismatch_throwsException() {
        String differentToken = "different-refresh-token";

        given(jwtTokenProvider.getAccountIdFromJWT(differentToken)).willReturn(ACCOUNT_ID);
        given(refreshTokenRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.logout(differentToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 일치하지 않습니다.");

        then(refreshTokenRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("만료된 refresh token - 예외 발생")
    void logout_expiredToken_throwsException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .accountId(ACCOUNT_ID)
                .token(REFRESH_TOKEN)
                .expiryDate(new Date(System.currentTimeMillis() - 60_000))
                .build();

        given(jwtTokenProvider.getAccountIdFromJWT(REFRESH_TOKEN)).willReturn(ACCOUNT_ID);
        given(refreshTokenRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 만료되었습니다.");

        then(refreshTokenRepository).should(never()).deleteById(any());
    }
}
