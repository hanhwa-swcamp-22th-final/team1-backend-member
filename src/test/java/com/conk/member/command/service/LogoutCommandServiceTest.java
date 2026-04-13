package com.conk.member.command.service;

import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutCommandServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks AuthService authService;

    private static final String ACCOUNT_ID = "ACC-001";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("로그아웃 성공 - accountId 기준으로 refresh token 삭제")
    void logout_success() {
        authService.logout(ACCOUNT_ID);

        then(refreshTokenRepository).should().deleteById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("accountId가 없으면 예외 발생")
    void logout_missingAccountId_throwsException() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));

        then(refreshTokenRepository).should(never()).deleteById(any());
    }

}