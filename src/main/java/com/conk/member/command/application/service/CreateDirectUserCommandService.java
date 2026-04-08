package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional
public class CreateDirectUserCommandService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final WarehouseService warehouseService;

    public CreateDirectUserCommandService(AccountRepository accountRepository,
                                          RoleRepository roleRepository,
                                          PasswordService passwordService,
                                          WarehouseService warehouseService) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
        this.warehouseService = warehouseService;
    }

    public CreateDirectUserResponse createDirect(CreateDirectUserRequest request) {
        validateDuplicateWorkerCode(request.getWorkerCode());
        if (StringUtils.hasText(request.getEmail())) {
            validateDuplicateEmail(request.getEmail());
        }
        if (!warehouseService.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }

        Role role = getRole(RoleName.WAREHOUSE_WORKER);
        Account account = new Account();
        account.setAccountId(generateId("ACC"));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setWarehouseId(request.getWarehouseId());
        account.setAccountName(request.getName());
        account.setWorkerCode(request.getWorkerCode());
        account.setEmail(request.getEmail());
        account.setPhoneNo(request.getPhoneNo());
        account.setPasswordHash(passwordService.encode(request.getPassword()));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);

        return createDirectUserResponse(account, role.getRoleName().name());
    }

    private void validateDuplicateWorkerCode(String workerCode) {
        if (accountRepository.existsByWorkerCode(workerCode)) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private CreateDirectUserResponse createDirectUserResponse(Account account, String roleName) {
        CreateDirectUserResponse response = new CreateDirectUserResponse();
        response.setId(account.getAccountId());
        response.setRole(roleName);
        response.setName(account.getAccountName());
        response.setWorkerCode(account.getWorkerCode());
        response.setTenantId(account.getTenantId());
        response.setWarehouseId(account.getWarehouseId());
        response.setAccountStatus(account.getAccountStatus().name());
        return response;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
