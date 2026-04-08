package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResetPasswordCommandService {

    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final MailService mailService;

    public ResetPasswordCommandService(AccountRepository accountRepository,
                                       PasswordService passwordService,
                                       MailService mailService) {
        this.accountRepository = accountRepository;
        this.passwordService = passwordService;
        this.mailService = mailService;
    }

    public SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);
        mailService.sendTemporaryPassword(account.getEmail(), temporaryPassword);

        return createSimpleUserStatusResponse(account);
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private SimpleUserStatusResponse createSimpleUserStatusResponse(Account account) {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }
}
