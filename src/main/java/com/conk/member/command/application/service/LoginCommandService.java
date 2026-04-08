package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@Transactional
public class LoginCommandService {

    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TenantRepository tenantRepository;

    public LoginCommandService(AccountRepository accountRepository,
                               PasswordService passwordService,
                               JwtTokenProvider jwtTokenProvider,
                               RefreshTokenRepository refreshTokenRepository,
                               TenantRepository tenantRepository) {
        this.accountRepository = accountRepository;
        this.passwordService = passwordService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tenantRepository = tenantRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Account account = findLoginAccount(request.getEmailOrWorkerCode());
        validatePassword(request.getPassword(), account.getPasswordHash());
        validateAccountIsActive(account);

        account.successLogin();
        accountRepository.save(account);

        String accessToken = jwtTokenProvider.createToken(account);
        String refreshToken = jwtTokenProvider.createRefreshToken(account);

        saveRefreshToken(account.getAccountId(), refreshToken);
        return createLoginResponse(account, accessToken, refreshToken);
    }

    private Account findLoginAccount(String emailOrWorkerCode) {
        return accountRepository.findByEmail(emailOrWorkerCode)
                .or(() -> accountRepository.findByWorkerCode(emailOrWorkerCode))
                .orElseThrow(() -> new MemberException(ErrorCode.INVALID_CREDENTIALS));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordService.matches(rawPassword, encodedPassword)) {
            throw new MemberException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void validateAccountIsActive(Account account) {
        if (account.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new MemberException(ErrorCode.FORBIDDEN, "비활성화된 계정입니다.");
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
