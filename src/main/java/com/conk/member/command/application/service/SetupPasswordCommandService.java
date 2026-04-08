package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SetupPasswordCommandService {

    private final TokenService tokenService;
    private final MemberTokenRepository memberTokenRepository;
    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final TenantRepository tenantRepository;

    public SetupPasswordCommandService(TokenService tokenService,
                                      MemberTokenRepository memberTokenRepository,
                                      AccountRepository accountRepository,
                                      PasswordService passwordService,
                                      TenantRepository tenantRepository) {
        this.tokenService = tokenService;
        this.memberTokenRepository = memberTokenRepository;
        this.accountRepository = accountRepository;
        this.passwordService = passwordService;
        this.tenantRepository = tenantRepository;
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

        return createSetupPasswordResponse(account);
    }

    private void validateSetupToken(MemberToken memberToken) {
        if (Boolean.TRUE.equals(memberToken.getIsUsed())) {
            throw new MemberException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (memberToken.isExpired()) {
            throw new MemberException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private SetupPasswordResponse createSetupPasswordResponse(Account account) {
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
}
