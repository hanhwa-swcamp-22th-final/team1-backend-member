package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.UpdateAdminUserRequest;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.UpdateAdminUserResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.infrastructure.service.MailService;
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
public class AccountCommandService {

    private final AccountRepository accountRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final SellerRepository sellerRepository;
    private final PasswordService passwordService;
    private final MailService mailService;
    private final WarehouseService warehouseService;

    public AccountCommandService(AccountRepository accountRepository,
                                 InvitationRepository invitationRepository,
                                 RoleRepository roleRepository,
                                 SellerRepository sellerRepository,
                                 PasswordService passwordService,
                                 MailService mailService,
                                 WarehouseService warehouseService) {
        this.accountRepository = accountRepository;
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.sellerRepository = sellerRepository;
        this.passwordService = passwordService;
        this.mailService = mailService;
        this.warehouseService = warehouseService;
    }

    public InviteAccountResponse invite(InviteAccountRequest request, String inviterAccountId) {
        RoleName roleName = parseRoleName(request.getRole());
        if (!StringUtils.hasText(inviterAccountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "초대 요청자를 확인할 수 없습니다.");
        }

        validateInviteRole(roleName);
        validateDuplicateEmail(request.getEmail());
        validateInviteReference(roleName, request);

        Role role = getRole(roleName);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        Account invitedAccount = new Account();
        invitedAccount.setAccountId(generateId("ACC"));
        invitedAccount.setRole(role);
        invitedAccount.setTenantId(request.getTenantId());
        invitedAccount.setSellerId(request.getSellerId());
        invitedAccount.setWarehouseId(request.getWarehouseId());
        invitedAccount.setAccountName(request.getName());
        invitedAccount.setEmail(request.getEmail());
        invitedAccount.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(invitedAccount);

        Invitation invitation = new Invitation();
        invitation.setInvitationId(generateId("INV"));
        invitation.setInviterAccountId(inviterAccountId);
        invitation.setInviteeAccountId(invitedAccount.getAccountId());
        invitation.setTargetRoleId(role.getRoleId());
        invitation.setTenantId(request.getTenantId());
        invitation.setSellerId(request.getSellerId());
        invitation.setWarehouseId(request.getWarehouseId());
        invitation.setInviteEmail(request.getEmail());
        invitation.markPending();
        invitationRepository.save(invitation);

        mailService.sendTemporaryPassword(request.getEmail(), temporaryPassword);
        return buildInviteResponse(invitedAccount, invitation, roleName.name());
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

        return buildCreateDirectUserResponse(account, role.getRoleName().name());
    }

    public CreateAdminUserResponse createAdminUser(CreateAdminUserRequest request) {
        if (!RoleName.MASTER_ADMIN.name().equals(request.getRole())) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "REQ-004는 MASTER_ADMIN 추가 발급만 허용합니다.");
        }

        validateDuplicateEmail(request.getEmail());

        Role role = getRole(RoleName.MASTER_ADMIN);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        Account account = new Account();
        account.setAccountId(generateId("ACC"));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setAccountName(request.getName());
        account.setEmail(request.getEmail());
        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);

        Invitation invitation = new Invitation();
        invitation.setInvitationId(generateId("INV"));
        invitation.setInviteeAccountId(account.getAccountId());
        invitation.setTargetRoleId(role.getRoleId());
        invitation.setTenantId(request.getTenantId());
        invitation.setInviteEmail(request.getEmail());
        invitation.markPending();
        invitationRepository.save(invitation);

        mailService.sendTemporaryPassword(request.getEmail(), temporaryPassword);

        CreateAdminUserResponse response = new CreateAdminUserResponse();
        response.setId(account.getAccountId());
        response.setTenantId(account.getTenantId());
        response.setName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setRole(role.getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setInvitationId(invitation.getInvitationId());
        return response;
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

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "유효하지 않은 역할입니다.");
        }
    }

    private void validateInviteRole(RoleName roleName) {
        if (roleName != RoleName.WAREHOUSE_MANAGER && roleName != RoleName.SELLER) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "MEM-005는 WAREHOUSE_MANAGER/SELLER 초대만 허용합니다.");
        }
    }

    private void validateInviteReference(RoleName roleName, InviteAccountRequest request) {
        if (roleName == RoleName.WAREHOUSE_MANAGER && !warehouseService.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }
        if (roleName == RoleName.SELLER && sellerRepository.findById(request.getSellerId()).isEmpty()) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 셀러입니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateWorkerCode(String workerCode) {
        if (accountRepository.existsByWorkerCode(workerCode)) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        }
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

    private InviteAccountResponse buildInviteResponse(Account account, Invitation invitation, String roleName) {
        InviteAccountResponse response = new InviteAccountResponse();
        response.setInvitationId(invitation.getInvitationId());
        response.setRole(roleName);
        response.setTenantId(invitation.getTenantId());
        response.setSellerId(invitation.getSellerId());
        response.setWarehouseId(invitation.getWarehouseId());
        response.setName(account.getAccountName());
        response.setEmail(invitation.getInviteEmail());
        response.setInviteStatus(invitation.getInviteStatus().name());
        response.setInviteSentAt(invitation.getInviteSentAt());
        return response;
    }

    private CreateDirectUserResponse buildCreateDirectUserResponse(Account account, String roleName) {
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

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
