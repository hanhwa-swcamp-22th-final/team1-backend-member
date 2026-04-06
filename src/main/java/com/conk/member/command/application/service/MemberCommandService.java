package com.conk.member.command.application.service;

/*
 * 멤버 인증/인가 command 로직을 한곳에 모은 서비스다.
 * 로그인, 최초 비밀번호 설정, 초대, 직접 발급, 업체 등록, 총괄관리자 추가 발급, RBAC 수정까지 담당한다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.domain.aggregate.*;
import com.conk.member.command.domain.enums.*;
import com.conk.member.command.domain.repository.*;
import com.conk.member.command.infrastructure.service.*;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final SellerRepository sellerRepository;
    private final InvitationRepository invitationRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RolePermissionHistoryRepository rolePermissionHistoryRepository;
    private final PasswordSupport passwordSupport;
    private final TokenSupport tokenSupport;
    private final MailSupport mailSupport;
    private final WarehouseSupport warehouseSupport;

    @Transactional(readOnly = true)
    public MemberResponses.LoginResponse login(MemberRequests.LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmailOrWorkerCode())
            .or(() -> accountRepository.findByWorkerCode(request.getEmailOrWorkerCode()))
            .orElseThrow(() -> new MemberException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordSupport.matches(request.getPassword(), account.getPasswordHash())) {
            throw new MemberException(ErrorCode.INVALID_CREDENTIALS);
        }
        account.successLogin();
        accountRepository.save(account);

        MemberResponses.LoginResponse response = new MemberResponses.LoginResponse();
        response.setToken(tokenSupport.createAccessToken(account.getAccountId(), account.getRole().getRoleName().name()));
        response.setId(account.getAccountId());
        response.setEmail(account.getEmail());
        response.setName(account.getAccountName());
        response.setRole(account.getRole().getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setTenantId(account.getTenantId());
        response.setSellerId(account.getSellerId());
        response.setWarehouseId(account.getWarehouseId());
        if (StringUtils.hasText(account.getTenantId())) {
            tenantRepository.findById(account.getTenantId()).ifPresent(t -> response.setTenantName(t.getTenantName()));
        }
        return response;
    }

    public MemberResponses.SetupPasswordResponse setupPassword(MemberRequests.SetupPasswordRequest request) {
        MemberToken token = memberTokenRepository.findByTokenHash(tokenSupport.hash(request.getSetupToken()))
            .orElseThrow(() -> new MemberException(ErrorCode.UNAUTHORIZED));
        if (Boolean.TRUE.equals(token.getIsUsed())) {
            throw new MemberException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (token.isExpired()) {
            throw new MemberException(ErrorCode.TOKEN_EXPIRED);
        }
        Account account = accountRepository.findById(token.getAccountId())
            .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        account.changePassword(passwordSupport.encode(request.getNewPassword()));
        accountRepository.save(account);
        token.use();
        memberTokenRepository.save(token);

        MemberResponses.SetupPasswordResponse response = new MemberResponses.SetupPasswordResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountStatus(account.getAccountStatus().name());
        response.setPasswordChangedAt(account.getPasswordChangedAt());
        if (account.isRole(RoleName.MASTER_ADMIN) && StringUtils.hasText(account.getTenantId())) {
            Tenant tenant = tenantRepository.findById(account.getTenantId()).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
            if (tenant.getStatus() == TenantStatus.SETTING) {
                tenant.activate();
                tenantRepository.save(tenant);
            }
            response.setTenantStatus(tenant.getStatus().name());
            response.setActivatedAt(tenant.getActivatedAt());
        }
        return response;
    }

    public MemberResponses.InviteAccountResponse invite(MemberRequests.InviteAccountRequest request, String inviterAccountId) {
        RoleName roleName = RoleName.valueOf(request.getRole());
        if (roleName != RoleName.WAREHOUSE_MANAGER && roleName != RoleName.SELLER) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "MEM-005는 WAREHOUSE_MANAGER/SELLER 초대만 허용합니다.");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (roleName == RoleName.WAREHOUSE_MANAGER && !warehouseSupport.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }
        if (roleName == RoleName.SELLER && sellerRepository.findById(request.getSellerId()).isEmpty()) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 셀러입니다.");
        }
        Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        String tempPassword = passwordSupport.generateTemporaryPassword();
        Account account = new Account();
        account.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setSellerId(request.getSellerId());
        account.setWarehouseId(request.getWarehouseId());
        account.setAccountName(request.getName());
        account.setEmail(request.getEmail());
        account.applyTemporaryPassword(passwordSupport.encode(tempPassword));
        accountRepository.save(account);

        Invitation invitation = new Invitation();
        invitation.setInvitationId("INV-" + UUID.randomUUID().toString().substring(0, 8));
        invitation.setInviterAccountId(inviterAccountId);
        invitation.setInviteeAccountId(account.getAccountId());
        invitation.setTargetRoleId(role.getRoleId());
        invitation.setTenantId(request.getTenantId());
        invitation.setSellerId(request.getSellerId());
        invitation.setWarehouseId(request.getWarehouseId());
        invitation.setInviteEmail(request.getEmail());
        invitation.markPending();
        invitationRepository.save(invitation);
        mailSupport.sendTemporaryPassword(request.getEmail(), tempPassword);

        MemberResponses.InviteAccountResponse response = new MemberResponses.InviteAccountResponse();
        response.setInvitationId(invitation.getInvitationId());
        response.setRole(role.getRoleName().name());
        response.setTenantId(invitation.getTenantId());
        response.setSellerId(invitation.getSellerId());
        response.setWarehouseId(invitation.getWarehouseId());
        response.setName(account.getAccountName());
        response.setEmail(invitation.getInviteEmail());
        response.setInviteStatus(invitation.getInviteStatus().name());
        response.setInviteSentAt(invitation.getInviteSentAt());
        return response;
    }

    public MemberResponses.SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        account.applyTemporaryPassword(passwordSupport.encode(passwordSupport.generateTemporaryPassword()));
        accountRepository.save(account);
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
    }

    public MemberResponses.SimpleUserStatusResponse deactivate(String userId) {
        Account account = getAccount(userId);
        validateLastActiveMasterAdmin(account, AccountStatus.INACTIVE.name());
        account.deactivate();
        accountRepository.save(account);
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        return response;
    }

    public MemberResponses.SimpleUserStatusResponse reactivate(String userId) {
        Account account = getAccount(userId);
        account.reactivate();
        accountRepository.save(account);
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        return response;
    }

    public MemberResponses.CreateDirectUserResponse createDirect(MemberRequests.CreateDirectUserRequest request) {
        if (accountRepository.existsByWorkerCode(request.getWorkerCode())) throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        if (StringUtils.hasText(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        if (!warehouseSupport.exists(request.getWarehouseId())) throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        Role role = roleRepository.findByRoleName(RoleName.WAREHOUSE_WORKER).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        Account account = new Account();
        account.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setWarehouseId(request.getWarehouseId());
        account.setAccountName(request.getName());
        account.setWorkerCode(request.getWorkerCode());
        account.setEmail(request.getEmail());
        account.setPhoneNo(request.getPhoneNo());
        account.setPasswordHash(passwordSupport.encode(request.getPassword()));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);
        MemberResponses.CreateDirectUserResponse response = new MemberResponses.CreateDirectUserResponse();
        response.setId(account.getAccountId());
        response.setRole(role.getRoleName().name());
        response.setName(account.getAccountName());
        response.setWorkerCode(account.getWorkerCode());
        response.setTenantId(account.getTenantId());
        response.setWarehouseId(account.getWarehouseId());
        response.setAccountStatus(account.getAccountStatus().name());
        return response;
    }

    public MemberResponses.CreateCompanyResponse createCompany(MemberRequests.CreateCompanyRequest request) {
        if (accountRepository.existsByEmail(request.getMasterAdminEmail())) throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-" + UUID.randomUUID().toString().substring(0, 8));
        tenant.setTenantCode("TEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tenant.setTenantName(request.getTenantName());
        tenant.setRepresentativeName(request.getRepresentativeName());
        tenant.setBusinessNo(request.getBusinessNo());
        tenant.setPhoneNo(request.getPhoneNo());
        tenant.setEmail(request.getEmail());
        tenant.setAddress(request.getAddress());
        tenant.setTenantType(request.getTenantType());
        tenant.setStatus(TenantStatus.SETTING);
        tenantRepository.save(tenant);

        Role role = roleRepository.findByRoleName(RoleName.MASTER_ADMIN).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        Account admin = new Account();
        admin.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        admin.setRole(role);
        admin.setTenantId(tenant.getTenantId());
        admin.setAccountName(request.getMasterAdminName());
        admin.setEmail(request.getMasterAdminEmail());
        admin.setAccountStatus(AccountStatus.TEMP_PASSWORD);
        admin.setIsTemporaryPassword(Boolean.TRUE);
        accountRepository.save(admin);

        String rawSetupToken = issueSetupToken(admin.getAccountId());
        mailSupport.sendSetupLink(admin.getEmail(), rawSetupToken);

        MemberResponses.CreateCompanyResponse response = new MemberResponses.CreateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setMasterAdminUserId(admin.getAccountId());
        response.setMasterAdminEmail(admin.getEmail());
        return response;
    }

    public MemberResponses.CreateAdminUserResponse createAdminUser(MemberRequests.CreateAdminUserRequest request) {
        if (!RoleName.MASTER_ADMIN.name().equals(request.getRole())) throw new MemberException(ErrorCode.BAD_REQUEST, "REQ-004는 MASTER_ADMIN 추가 발급만 허용합니다.");
        if (accountRepository.existsByEmail(request.getEmail())) throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        Role role = roleRepository.findByRoleName(RoleName.MASTER_ADMIN).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        String tempPassword = passwordSupport.generateTemporaryPassword();
        Account account = new Account();
        account.setAccountId("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setAccountName(request.getName());
        account.setEmail(request.getEmail());
        account.applyTemporaryPassword(passwordSupport.encode(tempPassword));
        accountRepository.save(account);
        Invitation invitation = new Invitation();
        invitation.setInvitationId("INV-" + UUID.randomUUID().toString().substring(0, 8));
        invitation.setInviteeAccountId(account.getAccountId());
        invitation.setTargetRoleId(role.getRoleId());
        invitation.setTenantId(request.getTenantId());
        invitation.setInviteEmail(request.getEmail());
        invitation.markPending();
        invitationRepository.save(invitation);
        mailSupport.sendTemporaryPassword(request.getEmail(), tempPassword);
        MemberResponses.CreateAdminUserResponse response = new MemberResponses.CreateAdminUserResponse();
        response.setId(account.getAccountId());
        response.setTenantId(account.getTenantId());
        response.setName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setRole(role.getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setInvitationId(invitation.getInvitationId());
        return response;
    }

    public MemberResponses.UpdateAdminUserResponse updateAdminUser(String id, MemberRequests.UpdateAdminUserRequest request) {
        Account account = getAccount(id);
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(account.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateLastActiveMasterAdmin(account, request.getStatus());
            account.setAccountStatus(AccountStatus.valueOf(request.getStatus()));
        }
        if (StringUtils.hasText(request.getName())) account.setAccountName(request.getName());
        if (StringUtils.hasText(request.getEmail())) account.setEmail(request.getEmail());
        accountRepository.save(account);
        MemberResponses.UpdateAdminUserResponse response = new MemberResponses.UpdateAdminUserResponse();
        response.setId(account.getAccountId());
        response.setTenantId(account.getTenantId());
        response.setName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setRole(account.getRole().getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }

    public MemberResponses.CreateSellerResponse createSeller(MemberRequests.CreateSellerRequest request) {
        if (request.getWarehouseIds() != null) {
            for (String warehouseId : request.getWarehouseIds()) {
                if (!warehouseSupport.exists(warehouseId)) throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
            }
        }
        Seller seller = new Seller();
        seller.setSellerId("SELLER-" + UUID.randomUUID().toString().substring(0, 8));
        seller.setTenantId(request.getTenantId());
        seller.setSellerInfo(request.getSellerInfo());
        seller.setBrandNameKo(request.getBrandNameKo());
        seller.setBrandNameEn(request.getBrandNameEn());
        seller.setRepresentativeName(request.getRepresentativeName());
        seller.setBusinessNo(request.getBusinessNo());
        seller.setPhoneNo(request.getPhoneNo());
        seller.setEmail(request.getEmail());
        seller.setCategoryName(request.getCategoryName());
        seller.setStatus("ACTIVE");
        seller.setCustomerCode("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sellerRepository.save(seller);
        MemberResponses.CreateSellerResponse response = new MemberResponses.CreateSellerResponse();
        response.setId(seller.getSellerId());
        response.setCustomerCode(seller.getCustomerCode());
        response.setBrandNameKo(seller.getBrandNameKo());
        response.setStatus(seller.getStatus());
        response.setCreatedAt(seller.getCreatedAt());
        return response;
    }

    public MemberResponses.RolePermissionUpdateResponse updateRolePermissions(String roleId, MemberRequests.UpdateRolePermissionsRequest request, String changedBy) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
        int updatedCount = 0;
        if (request.getPermissions() != null) {
            for (MemberRequests.UpdateRolePermissionsRequest.PermissionUpdate update : request.getPermissions()) {
                RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, update.getPermissionId()).orElseGet(RolePermission::new);
                Integer beforeRead = rolePermission.getCanRead();
                Integer beforeWrite = rolePermission.getCanWrite();
                Integer beforeDelete = rolePermission.getCanDelete();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(update.getPermissionId());
                rolePermission.setIsEnabled(update.getIsEnabled());
                rolePermission.setCanRead(update.getCanRead());
                rolePermission.setCanWrite(update.getCanWrite());
                rolePermission.setCanDelete(update.getCanDelete());
                RolePermission saved = rolePermissionRepository.save(rolePermission);
                RolePermissionHistory history = new RolePermissionHistory();
                history.setPermissionChangeId("HIS-" + UUID.randomUUID().toString().substring(0, 8));
                history.setRolePermissionId(saved.getRolePermissionPk());
                history.setRoleId(roleId);
                history.setPermissionId(saved.getPermissionId());
                history.setBeforeCanRead(beforeRead);
                history.setBeforeCanWrite(beforeWrite);
                history.setBeforeCanDelete(beforeDelete);
                history.setAfterCanRead(saved.getCanRead());
                history.setAfterCanWrite(saved.getCanWrite());
                history.setAfterCanDelete(saved.getCanDelete());
                history.setChangedBy(changedBy);
                history.setChangedAt(LocalDateTime.now());
                rolePermissionHistoryRepository.save(history);
                updatedCount++;
            }
        }
        MemberResponses.RolePermissionUpdateResponse response = new MemberResponses.RolePermissionUpdateResponse();
        response.setRoleId(roleId);
        response.setUpdatedCount(updatedCount);
        return response;
    }

    private Account getAccount(String id) {
        return accountRepository.findById(id).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private void validateLastActiveMasterAdmin(Account account, String targetStatus) {
        if (account.isRole(RoleName.MASTER_ADMIN) && account.getAccountStatus() == AccountStatus.ACTIVE && AccountStatus.INACTIVE.name().equals(targetStatus)) {
            long activeCount = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(account.getTenantId(), RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
            if (activeCount <= 1) throw new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
        }
    }

    private String issueSetupToken(String accountId) {
        String raw = tokenSupport.createSetupToken();
        MemberToken token = new MemberToken();
        token.setTokenId("TOK-" + UUID.randomUUID().toString().substring(0, 8));
        token.setAccountId(accountId);
        token.setTokenHash(tokenSupport.hash(raw));
        token.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setIsUsed(Boolean.FALSE);
        memberTokenRepository.save(token);
        return raw;
    }
}
