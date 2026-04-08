package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.UpdateAdminUserRequest;
import com.conk.member.command.application.dto.response.UpdateAdminUserResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UpdateAdminUserCommandService {

    private final AccountRepository accountRepository;

    public UpdateAdminUserCommandService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UpdateAdminUserResponse updateAdminUser(String id, UpdateAdminUserRequest request) {
        Account account = getAccount(id);

        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(account.getEmail())
                && accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (StringUtils.hasText(request.getStatus())) {
            validateLastActiveMasterAdmin(account, request.getStatus());
            account.setAccountStatus(AccountStatus.valueOf(request.getStatus()));
        }
        if (StringUtils.hasText(request.getName())) {
            account.setAccountName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            account.setEmail(request.getEmail());
        }

        accountRepository.save(account);

        UpdateAdminUserResponse response = new UpdateAdminUserResponse();
        response.setId(account.getAccountId());
        response.setTenantId(account.getTenantId());
        response.setName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setRole(account.getRole().getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private void validateLastActiveMasterAdmin(Account account, String targetStatus) {
        if (account.isRole(RoleName.MASTER_ADMIN)
                && account.getAccountStatus() == AccountStatus.ACTIVE
                && AccountStatus.INACTIVE.name().equals(targetStatus)) {
            long activeMasterAdminCount = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                    account.getTenantId(),
                    RoleName.MASTER_ADMIN,
                    AccountStatus.ACTIVE
            );
            if (activeMasterAdminCount <= 1) {
                throw new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
            }
        }
    }
}
