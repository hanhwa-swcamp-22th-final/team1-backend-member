package com.conk.member.command.application.service;

import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class LogoutCommandService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutCommandService(JwtTokenProvider jwtTokenProvider,
                                RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void logout(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);
        String accountId = jwtTokenProvider.getAccountIdFromJWT(refreshToken);
        RefreshToken storedRefreshToken = getStoredRefreshToken(accountId);
        validateStoredRefreshToken(storedRefreshToken, refreshToken);
        refreshTokenRepository.deleteById(accountId);
    }

    private RefreshToken getStoredRefreshToken(String accountId) {
        return refreshTokenRepository.findById(accountId)
                .orElseThrow(() -> new BadCredentialsException("Refresh Token을 찾을 수 없습니다."));
    }

    private void validateStoredRefreshToken(RefreshToken storedRefreshToken, String providedRefreshToken) {
        if (!storedRefreshToken.getToken().equals(providedRefreshToken)) {
            throw new BadCredentialsException("Refresh Token이 일치하지 않습니다.");
        }
        if (storedRefreshToken.getExpiryDate().before(new Date())) {
            throw new BadCredentialsException("Refresh Token이 만료되었습니다.");
        }
    }
}
