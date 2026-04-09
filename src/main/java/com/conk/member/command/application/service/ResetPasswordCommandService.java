package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.command.infrastructure.mail.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ResetPasswordCommandService {

  private final AccountRepository accountRepository;
  private final TenantRepository tenantRepository;
  private final PasswordService passwordService;
  private final MailService mailService;

    public ResetPasswordCommandService(AccountRepository accountRepository,
                                       TenantRepository tenantRepository,
                                       PasswordService passwordService,
                                       MailService mailService) {
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
        this.mailService = mailService;
    }

    public SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);

        if (StringUtils.hasText(account.getEmail())) {
            String companyName = resolveCompanyName(account.getTenantId());
            String roleName = account.getRole() != null ? account.getRole().getRoleName().name() : "";
            mailService.sendPasswordResetMail(
                    account.getEmail(),
                    account.getAccountName(),
                    roleName,
                    companyName,
                    temporaryPassword
            );
        }

        return createSimpleUserStatusResponse(account);
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String resolveCompanyName(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return "";
        }
        return tenantRepository.findById(tenantId)
                .map(Tenant::getTenantName)
                .orElse("");
    }

    private SimpleUserStatusResponse createSimpleUserStatusResponse(Account account) {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }
}
