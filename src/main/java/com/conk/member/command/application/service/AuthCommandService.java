package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@Transactional
public class AuthCommandService {

    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;

    public AuthCommandService(AccountRepository accountRepository,
                              TenantRepository tenantRepository,
                              MemberTokenRepository memberTokenRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              PasswordService passwordService,
                              TokenService tokenService,
                              JwtTokenProvider jwtTokenProvider,
                              MailService mailService) {
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
        this.memberTokenRepository = memberTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailService = mailService;
    }

    public LoginResponse login(LoginRequest request) {
        Account account = findLoginAccount(request.getEmailOrWorkerCode());
        validatePassword(request.getPassword(), account.getPasswordHash());
        validateAccountIsActive(account);

        account.successLogin();
        accountRepository.save(account);

        String roleName = account.getRole().getRoleName().name();
        String accessToken = jwtTokenProvider.createToken(account);
        String refreshToken = jwtTokenProvider.createRefreshToken(account);

        saveRefreshToken(account.getAccountId(), refreshToken);
        return buildLoginResponse(account, accessToken, refreshToken);
    }

    public SetupPasswordResponse setupPassword(SetupPasswordRequest request) {
        String tokenHash = tokenService.hash(request.getSetupToken());
        MemberToken memberToken = memberTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new MemberException(ErrorCode.UNAUTHORIZED));

        validateSetupToken(memberToken);

        Account account = getAccount(memberToken.getAccountId());
        account.changePassword(passwordService.encode(request.getNewPassword()));
        accountRepository.save(account);

        memberToken.use();
        memberTokenRepository.save(memberToken);

        return buildSetupPasswordResponse(account);
    }

    public SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);

        if (StringUtils.hasText(account.getEmail())) {
            mailService.sendTemporaryPassword(account.getEmail(), temporaryPassword);
        }

        return buildSimpleUserStatusResponse(account);
    }

    public SimpleUserStatusResponse deactivate(String userId) {
        Account account = getAccount(userId);
        validateLastActiveMasterAdmin(account, AccountStatus.INACTIVE.name());

        account.deactivate();
        accountRepository.save(account);

        return buildSimpleUserStatusResponse(account);
    }

    public SimpleUserStatusResponse reactivate(String userId) {
        Account account = getAccount(userId);
        account.reactivate();
        accountRepository.save(account);
        return buildSimpleUserStatusResponse(account);
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

    private LoginResponse buildLoginResponse(Account account, String accessToken, String refreshToken) {
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

    private void validateSetupToken(MemberToken memberToken) {
        if (Boolean.TRUE.equals(memberToken.getIsUsed())) {
            throw new MemberException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (memberToken.isExpired()) {
            throw new MemberException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    private SetupPasswordResponse buildSetupPasswordResponse(Account account) {
        SetupPasswordResponse response = new SetupPasswordResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountStatus(account.getAccountStatus().name());
        response.setPasswordChangedAt(account.getPasswordChangedAt());

        if (account.isRole(RoleName.MASTER_ADMIN) && StringUtils.hasText(account.getTenantId())) {
            Tenant tenant = tenantRepository.findById(account.getTenantId())
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

            if (tenant.getStatus() == TenantStatus.SETTING) {
                tenant.activate();
                tenantRepository.save(tenant);
            }

            response.setTenantStatus(tenant.getStatus().name());
            response.setActivatedAt(tenant.getActivatedAt());
        }
        return response;
    }

    private SimpleUserStatusResponse buildSimpleUserStatusResponse(Account account) {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }

    private void validateLastActiveMasterAdmin(Account account, String targetStatus) {
        if (account.isRole(RoleName.MASTER_ADMIN)
                && account.getAccountStatus() == AccountStatus.ACTIVE
                && AccountStatus.INACTIVE.name().equals(targetStatus)) {
            long count = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                    account.getTenantId(), RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
            if (count <= 1) {
                throw new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
            }
        }
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }
}
