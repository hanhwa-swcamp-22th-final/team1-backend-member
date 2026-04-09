package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
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
        validateLastActiveMasterAdmin(account);
        account.deactivate();
        accountRepository.save(account);
        return createSimpleUserStatusResponse(account);
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private void validateLastActiveMasterAdmin(Account account) {
        if (account.isRole(RoleName.MASTER_ADMIN)
                && account.getAccountStatus() == AccountStatus.ACTIVE) {
            long count = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                    account.getTenantId(), RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
            if (count <= 1) {
                throw new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
            }
        }
    }

    private SimpleUserStatusResponse createSimpleUserStatusResponse(Account account) {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }
}
