package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@Transactional
public class RefreshTokenCommandService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;

    public RefreshTokenCommandService(JwtTokenProvider jwtTokenProvider,
                                      RefreshTokenRepository refreshTokenRepository,
                                      AccountRepository accountRepository,
                                      TenantRepository tenantRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
    }

    public LoginResponse refreshToken(String providedRefreshToken) {
        jwtTokenProvider.validateRefreshToken(providedRefreshToken);
        String accountId = jwtTokenProvider.getAccountIdFromJWT(providedRefreshToken);

        RefreshToken storedRefreshToken = getStoredRefreshToken(accountId);
        validateStoredRefreshToken(storedRefreshToken, providedRefreshToken);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createToken(account);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(account);

        saveRefreshToken(account.getAccountId(), newRefreshToken);
        return createLoginResponse(account, newAccessToken, newRefreshToken);
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

    private void saveRefreshToken(String accountId, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .accountId(accountId)
                .token(refreshToken)
                .expiryDate(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()))
                .build();
        refreshTokenRepository.save(token);
    }

    private LoginResponse createLoginResponse(Account account, String accessToken, String refreshToken) {
        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setId(account.getAccountId());
        response.setEmail(account.getEmail());
        response.setName(account.getAccountName());
        response.setRole(account.getRole().getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setTenantId(account.getTenantId());
        response.setSellerId(account.getSellerId());
        response.setWarehouseId(account.getWarehouseId());
        if (StringUtils.hasText(account.getTenantId())) {
            tenantRepository.findById(account.getTenantId())
                    .ifPresent(tenant -> response.setTenantName(tenant.getTenantName()));
        }
        return response;
    }
}
