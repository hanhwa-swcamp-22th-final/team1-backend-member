package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.InternalWorkerAccountCreateRequest;
import com.conk.member.command.application.dto.request.InternalWorkerAccountUpdateRequest;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.response.InternalWorkerAccountResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WMS 내부 작업자 계정 서비스다.
 */
@Service
@Transactional
public class InternalWorkerAccountService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SYSTEM_ACTOR = "internal-wms";

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;

    public InternalWorkerAccountService(AccountRepository accountRepository,
                                        RoleRepository roleRepository,
                                        PasswordService passwordService) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
    }

    @Transactional(readOnly = true)
    public List<InternalWorkerAccountResponse> getWorkers(String tenantId) {
        return accountRepository.findAllByTenantIdAndRoleRoleNameOrderByWorkerCodeAsc(tenantId, RoleName.WH_WORKER).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InternalWorkerAccountResponse getWorker(String tenantId, String workerId) {
        return accountRepository.findByTenantIdAndWorkerCode(tenantId, workerId)
                .map(this::toResponse)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND, "작업자를 찾을 수 없습니다."));
    }

    public InternalWorkerAccountResponse createWorker(String tenantId, InternalWorkerAccountCreateRequest request) {
        if (accountRepository.existsByTenantIdAndWorkerCode(tenantId, request.getId())) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        }
        if (hasText(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

        Role workerRole = roleRepository.findByRoleName(RoleName.WH_WORKER)
                .orElseThrow(() -> new MemberException(ErrorCode.INVALID_REFERENCE, "WH_WORKER 역할을 찾을 수 없습니다."));

        Account account = new Account();
        account.setAccountId("ACC-" + java.util.UUID.randomUUID());
        account.setRole(workerRole);
        account.setTenantId(tenantId);
        account.setWarehouseId(null);
        account.setSellerId(null);
        account.setAccountName(request.getName().trim());
        account.setWorkerCode(request.getId().trim());
        account.setEmail(trimToNull(request.getEmail()));
        account.setPasswordHash(passwordService.encode(request.getPassword().trim()));
        account.setAccountStatus(resolveStatus(request.getAccountStatus()));
        account.setIsTemporaryPassword(Boolean.FALSE);
        account.setCreatedBy(SYSTEM_ACTOR);
        account.setUpdatedBy(SYSTEM_ACTOR);

        return toResponse(accountRepository.save(account));
    }

    public InternalWorkerAccountResponse updateWorker(String tenantId,
                                                      String workerId,
                                                      InternalWorkerAccountUpdateRequest request) {
        Account account = accountRepository.findByTenantIdAndWorkerCode(tenantId, workerId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND, "작업자를 찾을 수 없습니다."));

        String email = trimToNull(request.getEmail());
        if (hasText(email) && !email.equals(account.getEmail()) && accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (hasText(request.getName())) {
            account.setAccountName(request.getName().trim());
        }
        if (request.getEmail() != null) {
            account.setEmail(email);
        }
        if (hasText(request.getAccountStatus())) {
            account.setAccountStatus(resolveStatus(request.getAccountStatus()));
        }
        account.setUpdatedBy(SYSTEM_ACTOR);

        return toResponse(accountRepository.save(account));
    }

    private InternalWorkerAccountResponse toResponse(Account account) {
        return InternalWorkerAccountResponse.builder()
                .id(account.getWorkerCode())
                .name(account.getAccountName())
                .email(account.getEmail())
                .accountStatus(account.getAccountStatus().name())
                .zones(List.of())
                .memo(null)
                .presenceStatus(account.getAccountStatus() == AccountStatus.INACTIVE ? "OFFLINE" : "IDLE")
                .registeredAt(account.getCreatedAt() == null ? null : account.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }

    private AccountStatus resolveStatus(String status) {
        if (!hasText(status)) {
            return AccountStatus.ACTIVE;
        }
        return AccountStatus.valueOf(status.trim().toUpperCase());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
