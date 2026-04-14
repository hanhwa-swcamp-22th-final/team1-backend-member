package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
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
public class UserService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;
    private final WarehouseService warehouseService;
    private final MailService mailService;

    public UserService(AccountRepository accountRepository,
                       RoleRepository roleRepository,
                       TenantRepository tenantRepository,
                       PasswordService passwordService,
                       WarehouseService warehouseService,
                       MailService mailService) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
        this.warehouseService = warehouseService;
        this.mailService = mailService;
    }

    // ─── createDirect ─────────────────────────────────────────────────────────

    public CreateDirectUserResponse createDirect(CreateDirectUserRequest request) {
        if (accountRepository.existsByWorkerCode(request.getWorkerCode())) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        }
        if (StringUtils.hasText(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (!warehouseService.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }

        Role role = getRole(RoleName.WH_WORKER);
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

        CreateDirectUserResponse response = new CreateDirectUserResponse();
        response.setId(account.getAccountId());
        response.setRole(role.getRoleName().name());
        response.setName(account.getAccountName());
        response.setWorkerCode(account.getWorkerCode());
        response.setTenantId(account.getTenantId());
        response.setWarehouseId(account.getWarehouseId());
        response.setAccountStatus(account.getAccountStatus().name());
        return response;
    }

    // ─── deactivate ───────────────────────────────────────────────────────────

    public SimpleUserStatusResponse deactivate(String userId) {
        Account account = getAccount(userId);

        if (account.isRole(RoleName.MASTER_ADMIN) && account.getAccountStatus() == AccountStatus.ACTIVE) {
            long count = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                    account.getTenantId(), RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
            if (count <= 1) {
                throw new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
            }
        }

        account.deactivate();
        accountRepository.save(account);
        return buildSimpleResponse(account);
    }

    // ─── reactivate ───────────────────────────────────────────────────────────

    public SimpleUserStatusResponse reactivate(String userId) {
        Account account = getAccount(userId);
        account.reactivate();
        accountRepository.save(account);
        return buildSimpleResponse(account);
    }

    // ─── resetPassword ────────────────────────────────────────────────────────

    public SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        String temporaryPassword = passwordService.generateTemporaryPassword();
        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);

        if (StringUtils.hasText(account.getEmail())) {
            String companyName = resolveCompanyName(account.getTenantId());
            String roleName = account.getRole() != null ? account.getRole().getRoleName().name() : "";
            mailService.sendPasswordResetMail(account.getEmail(), account.getAccountName(),
                    roleName, companyName, temporaryPassword);
        }

        return buildSimpleResponse(account);
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String resolveCompanyName(String tenantId) {
        if (!StringUtils.hasText(tenantId)) return "";
        return tenantRepository.findById(tenantId).map(Tenant::getTenantName).orElse("");
    }

    private SimpleUserStatusResponse buildSimpleResponse(Account account) {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
