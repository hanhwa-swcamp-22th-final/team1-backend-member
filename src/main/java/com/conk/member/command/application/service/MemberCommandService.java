package com.conk.member.command.application.service;

/*
 * member command 업무를 처리하는 서비스다.
 *
 * 이 클래스가 하는 일
 * - 로그인
 * - 최초 비밀번호 설정
 * - 초대 / 직접 발급
 * - 업체 생성 / 관리자 생성
 * - 권한 변경
 *
 * learned code 기준으로 컨트롤러는 요청만 받고,
 * 실제 업무 흐름은 이 서비스가 처리하도록 구성했다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.RolePermission;
import com.conk.member.command.domain.aggregate.RolePermissionHistory;
import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.enums.TokenType;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.RolePermissionHistoryRepository;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
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
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MailService mailService;
    private final WarehouseService warehouseService;

    public MemberCommandService(AccountRepository accountRepository,
                                TenantRepository tenantRepository,
                                SellerRepository sellerRepository,
                                InvitationRepository invitationRepository,
                                MemberTokenRepository memberTokenRepository,
                                RoleRepository roleRepository,
                                RolePermissionRepository rolePermissionRepository,
                                RolePermissionHistoryRepository rolePermissionHistoryRepository,
                                PasswordService passwordService,
                                TokenService tokenService,
                                JwtTokenProvider jwtTokenProvider,
                                RefreshTokenRepository refreshTokenRepository,
                                MailService mailService,
                                WarehouseService warehouseService) {
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
        this.sellerRepository = sellerRepository;
        this.invitationRepository = invitationRepository;
        this.memberTokenRepository = memberTokenRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.rolePermissionHistoryRepository = rolePermissionHistoryRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mailService = mailService;
        this.warehouseService = warehouseService;
    }

    public MemberResponses.LoginResponse login(MemberRequests.LoginRequest request) {
        Account account = findLoginAccount(request.getEmailOrWorkerCode());
        validatePassword(request.getPassword(), account.getPasswordHash());
        validateAccountIsActive(account);

        account.successLogin();
        accountRepository.save(account);

        String roleName = account.getRole().getRoleName().name();
        String accessToken = jwtTokenProvider.createToken(account.getAccountId(), roleName);
        String refreshToken = jwtTokenProvider.createRefreshToken(account.getAccountId(), roleName);

        saveRefreshToken(account.getAccountId(), refreshToken);
        return createLoginResponse(account, accessToken, refreshToken);
    }

    public MemberResponses.SetupPasswordResponse setupPassword(MemberRequests.SetupPasswordRequest request) {
        String tokenHash = tokenService.hash(request.getSetupToken());
        MemberToken memberToken = memberTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new MemberException(ErrorCode.UNAUTHORIZED));

        validateSetupToken(memberToken);

        Account account = getAccount(memberToken.getAccountId());
        account.changePassword(passwordService.encode(request.getNewPassword()));
        accountRepository.save(account);

        memberToken.use();
        memberTokenRepository.save(memberToken);

        return createSetupPasswordResponse(account);
    }

    public MemberResponses.InviteAccountResponse invite(MemberRequests.InviteAccountRequest request, String inviterAccountId) {
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
        return createInviteResponse(invitedAccount, invitation, roleName.name());
    }

    public MemberResponses.SimpleUserStatusResponse resetPassword(String userId) {
        Account account = getAccount(userId);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        account.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        accountRepository.save(account);

        if (StringUtils.hasText(account.getEmail())) {
            mailService.sendTemporaryPassword(account.getEmail(), temporaryPassword);
        }

        return createSimpleUserStatusResponse(account);
    }

    public MemberResponses.SimpleUserStatusResponse deactivate(String userId) {
        Account account = getAccount(userId);
        validateLastActiveMasterAdmin(account, AccountStatus.INACTIVE.name());

        account.deactivate();
        accountRepository.save(account);

        return createSimpleUserStatusResponse(account);
    }

    public MemberResponses.SimpleUserStatusResponse reactivate(String userId) {
        Account account = getAccount(userId);
        account.reactivate();
        accountRepository.save(account);
        return createSimpleUserStatusResponse(account);
    }

    public MemberResponses.CreateDirectUserResponse createDirect(MemberRequests.CreateDirectUserRequest request) {
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

    public MemberResponses.CreateCompanyResponse createCompany(MemberRequests.CreateCompanyRequest request) {
        validateDuplicateEmail(request.getMasterAdminEmail());

        Tenant tenant = new Tenant();
        tenant.setTenantId(generateId("TENANT"));
        tenant.setTenantCode(generateCode("TEN"));
        tenant.setTenantName(request.getTenantName());
        tenant.setRepresentativeName(request.getRepresentativeName());
        tenant.setBusinessNo(request.getBusinessNo());
        tenant.setPhoneNo(request.getPhoneNo());
        tenant.setEmail(request.getEmail());
        tenant.setAddress(request.getAddress());
        tenant.setTenantType(request.getTenantType());
        tenant.setStatus(TenantStatus.SETTING);
        tenantRepository.save(tenant);

        Role role = getRole(RoleName.MASTER_ADMIN);

        Account adminAccount = new Account();
        adminAccount.setAccountId(generateId("ACC"));
        adminAccount.setRole(role);
        adminAccount.setTenantId(tenant.getTenantId());
        adminAccount.setAccountName(request.getMasterAdminName());
        adminAccount.setEmail(request.getMasterAdminEmail());
        adminAccount.setAccountStatus(AccountStatus.TEMP_PASSWORD);
        adminAccount.setIsTemporaryPassword(Boolean.TRUE);
        accountRepository.save(adminAccount);

        String rawSetupToken = issueSetupToken(adminAccount.getAccountId());
        mailService.sendSetupLink(adminAccount.getEmail(), rawSetupToken);

        MemberResponses.CreateCompanyResponse response = new MemberResponses.CreateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setMasterAdminUserId(adminAccount.getAccountId());
        response.setMasterAdminEmail(adminAccount.getEmail());
        return response;
    }

    public MemberResponses.UpdateCompanyResponse updateCompany(String id, MemberRequests.UpdateCompanyRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (StringUtils.hasText(request.getTenantName())) {
            tenant.setTenantName(request.getTenantName());
        }
        if (StringUtils.hasText(request.getRepresentativeName())) {
            tenant.setRepresentativeName(request.getRepresentativeName());
        }
        if (StringUtils.hasText(request.getBusinessNo())) {
            tenant.setBusinessNo(request.getBusinessNo());
        }
        if (StringUtils.hasText(request.getPhoneNo())) {
            tenant.setPhoneNo(request.getPhoneNo());
        }
        if (StringUtils.hasText(request.getEmail())) {
            tenant.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAddress())) {
            tenant.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getTenantType())) {
            tenant.setTenantType(request.getTenantType());
        }
        if (StringUtils.hasText(request.getStatus())) {
            tenant.setStatus(TenantStatus.valueOf(request.getStatus()));
        }

        tenantRepository.save(tenant);

        MemberResponses.UpdateCompanyResponse response = new MemberResponses.UpdateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setUpdatedAt(tenant.getUpdatedAt());
        return response;
    }

    public MemberResponses.CreateAdminUserResponse createAdminUser(MemberRequests.CreateAdminUserRequest request) {
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
                if (!warehouseService.exists(warehouseId)) {
                    throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
                }
            }
        }

        Seller seller = new Seller();
        seller.setSellerId(generateId("SELLER"));
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
        seller.setCustomerCode(generateCode("CUST"));
        sellerRepository.save(seller);

        MemberResponses.CreateSellerResponse response = new MemberResponses.CreateSellerResponse();
        response.setId(seller.getSellerId());
        response.setCustomerCode(seller.getCustomerCode());
        response.setBrandNameKo(seller.getBrandNameKo());
        response.setStatus(seller.getStatus());
        response.setCreatedAt(seller.getCreatedAt());
        return response;
    }

    public MemberResponses.RolePermissionUpdateResponse updateRolePermissions(
            String roleId,
            MemberRequests.UpdateRolePermissionsRequest request,
            String changedBy) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (!StringUtils.hasText(changedBy)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "권한 변경 주체를 확인할 수 없습니다.");
        }

        validateRolePermissionScope(role);

        int updatedCount = 0;

        if (request.getPermissions() != null) {
            for (MemberRequests.UpdateRolePermissionsRequest.PermissionUpdate permissionUpdate : request.getPermissions()) {
                updatedCount += updateSingleRolePermission(roleId, permissionUpdate, changedBy);
            }
        }

        MemberResponses.RolePermissionUpdateResponse response = new MemberResponses.RolePermissionUpdateResponse();
        response.setRoleId(roleId);
        response.setUpdatedPermissionCount(updatedCount);
        response.setChangedAt(LocalDateTime.now());
        response.setChangedBy(changedBy);
        return response;
    }

    private int updateSingleRolePermission(String roleId,
                                           MemberRequests.UpdateRolePermissionsRequest.PermissionUpdate permissionUpdate,
                                           String changedBy) {
        RolePermission rolePermission = rolePermissionRepository
                .findByRoleIdAndPermissionId(roleId, permissionUpdate.getPermissionId())
                .orElseGet(RolePermission::new);

        Integer beforeRead = rolePermission.getCanRead();
        Integer beforeWrite = rolePermission.getCanWrite();
        Integer beforeDelete = rolePermission.getCanDelete();

        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionUpdate.getPermissionId());
        rolePermission.setIsEnabled(permissionUpdate.getIsEnabled());
        rolePermission.setCanRead(permissionUpdate.getCanRead());
        rolePermission.setCanWrite(permissionUpdate.getCanWrite());
        rolePermission.setCanDelete(permissionUpdate.getCanDelete());

        RolePermission savedRolePermission = rolePermissionRepository.save(rolePermission);
        boolean isNewPermission = beforeRead == null && beforeWrite == null && beforeDelete == null;

        RolePermissionHistory history = new RolePermissionHistory();
        history.setHistoryId(generateId("HIS"));
        history.setRoleId(roleId);
        history.setPermissionId(savedRolePermission.getPermissionId());
        history.setActionType(isNewPermission ? "CREATE" : "UPDATE");
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        rolePermissionHistoryRepository.save(history);

        return 1;
    }

    private Account findLoginAccount(String emailOrWorkerCode) {
        return accountRepository.findByEmail(emailOrWorkerCode)
                .or(() -> accountRepository.findByWorkerCode(emailOrWorkerCode))
                .orElseThrow(() -> new MemberException(ErrorCode.INVALID_CREDENTIALS));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordService.matches(rawPassword, encodedPassword)) {
            throw new MemberException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void validateAccountIsActive(Account account) {
        if (account.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new MemberException(ErrorCode.FORBIDDEN, "비활성화된 계정입니다.");
        }
    }

    private void saveRefreshToken(String accountId, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .accountId(accountId)
                .token(refreshToken)
                .expiryDate(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()))
                .build();

        refreshTokenRepository.save(token);
    }

    private MemberResponses.LoginResponse createLoginResponse(Account account,
                                                              String accessToken,
                                                              String refreshToken) {
        MemberResponses.LoginResponse response = new MemberResponses.LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setId(account.getAccountId());
        response.setEmail(account.getEmail());
        response.setName(account.getAccountName());
        response.setRole(account.getRole().getRoleName().name());
        response.setStatus(account.getAccountStatus().name());
        response.setTenantId(account.getTenantId());
        response.setSellerId(account.getSellerId());
        response.setWarehouseId(account.getWarehouseId());

        if (StringUtils.hasText(account.getTenantId())) {
            tenantRepository.findById(account.getTenantId())
                    .ifPresent(tenant -> response.setTenantName(tenant.getTenantName()));
        }

        return response;
    }

    private void validateSetupToken(MemberToken memberToken) {
        if (Boolean.TRUE.equals(memberToken.getIsUsed())) {
            throw new MemberException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (memberToken.isExpired()) {
            throw new MemberException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    private MemberResponses.SetupPasswordResponse createSetupPasswordResponse(Account account) {
        MemberResponses.SetupPasswordResponse response = new MemberResponses.SetupPasswordResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountStatus(account.getAccountStatus().name());
        response.setPasswordChangedAt(account.getPasswordChangedAt());

        if (account.isRole(RoleName.MASTER_ADMIN) && StringUtils.hasText(account.getTenantId())) {
            Tenant tenant = tenantRepository.findById(account.getTenantId())
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

            if (tenant.getStatus() == TenantStatus.SETTING) {
                tenant.activate();
                tenantRepository.save(tenant);
            }

            response.setTenantStatus(tenant.getStatus().name());
            response.setActivatedAt(tenant.getActivatedAt());
        }

        return response;
    }

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.valueOf(roleName);
        } catch (IllegalArgumentException exception) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "유효하지 않은 역할입니다.");
        }
    }

    private void validateInviteRole(RoleName roleName) {
        if (roleName != RoleName.WAREHOUSE_MANAGER && roleName != RoleName.SELLER) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "MEM-005는 WAREHOUSE_MANAGER/SELLER 초대만 허용합니다.");
        }
    }

    private void validateInviteReference(RoleName roleName, MemberRequests.InviteAccountRequest request) {
        if (roleName == RoleName.WAREHOUSE_MANAGER && !warehouseService.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }

        if (roleName == RoleName.SELLER && sellerRepository.findById(request.getSellerId()).isEmpty()) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 셀러입니다.");
        }
    }

    private MemberResponses.InviteAccountResponse createInviteResponse(Account account,
                                                                       Invitation invitation,
                                                                       String roleName) {
        MemberResponses.InviteAccountResponse response = new MemberResponses.InviteAccountResponse();
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

    private MemberResponses.SimpleUserStatusResponse createSimpleUserStatusResponse(Account account) {
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        response.setAccountStatus(account.getAccountStatus().name());
        response.setIsTemporaryPassword(account.getIsTemporaryPassword());
        return response;
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

    private MemberResponses.CreateDirectUserResponse createDirectUserResponse(Account account, String roleName) {
        MemberResponses.CreateDirectUserResponse response = new MemberResponses.CreateDirectUserResponse();
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

    private void validateRolePermissionScope(Role role) {
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER
                && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
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

    private String issueSetupToken(String accountId) {
        String rawToken = tokenService.createSetupToken();

        MemberToken memberToken = new MemberToken();
        memberToken.setTokenId(generateId("TOK"));
        memberToken.setAccountId(accountId);
        memberToken.setTokenHash(tokenService.hash(rawToken));
        memberToken.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        memberToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        memberToken.setIsUsed(Boolean.FALSE);
        memberTokenRepository.save(memberToken);

        return rawToken;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
