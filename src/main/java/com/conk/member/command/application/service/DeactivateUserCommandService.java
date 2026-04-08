package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeactivateUserCommandService {

    private final AccountRepository accountRepository;

    public DeactivateUserCommandService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public SimpleUserStatusResponse deactivate(String userId) {
        Account account = getAccount(userId);
        account.deactivate();
        accountRepository.save(account);
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
