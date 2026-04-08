package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.PermissionRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.RolePermissionHistoryRepository;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.jwt.JwtTokenProvider;

class MemberCommandService {
    private final LoginCommandService loginCommandService;
    private final SetupPasswordCommandService setupPasswordCommandService;
    private final InviteAccountCommandService inviteAccountCommandService;
    private final ResetPasswordCommandService resetPasswordCommandService;
    private final DeactivateUserCommandService deactivateUserCommandService;
    private final ReactivateUserCommandService reactivateUserCommandService;
    private final CreateDirectUserCommandService createDirectUserCommandService;
    private final CreateCompanyCommandService createCompanyCommandService;
    private final UpdateCompanyCommandService updateCompanyCommandService;
    private final CreateAdminUserCommandService createAdminUserCommandService;
    private final UpdateAdminUserCommandService updateAdminUserCommandService;
    private final CreateSellerCommandService createSellerCommandService;
    private final UpdateRolePermissionsCommandService updateRolePermissionsCommandService;

    MemberCommandService(AccountRepository accountRepository,
                         TenantRepository tenantRepository,
                         SellerRepository sellerRepository,
                         SellerWarehouseRepository sellerWarehouseRepository,
                         InvitationRepository invitationRepository,
                         MemberTokenRepository memberTokenRepository,
                         RoleRepository roleRepository,
                         RolePermissionRepository rolePermissionRepository,
                         RolePermissionHistoryRepository rolePermissionHistoryRepository,
                         PermissionRepository permissionRepository,
                         PasswordService passwordService,
                         TokenService tokenService,
                         JwtTokenProvider jwtTokenProvider,
                         RefreshTokenRepository refreshTokenRepository,
                         MailService mailService,
                         WarehouseService warehouseService) {
        this.loginCommandService = new LoginCommandService(
                accountRepository,
                passwordService,
                jwtTokenProvider,
                refreshTokenRepository,
                tenantRepository
        );
        this.setupPasswordCommandService = new SetupPasswordCommandService(
                tokenService,
                memberTokenRepository,
                accountRepository,
                passwordService,
                tenantRepository
        );
        this.inviteAccountCommandService = new InviteAccountCommandService(
                accountRepository,
                sellerRepository,
                invitationRepository,
                roleRepository,
                passwordService,
                mailService,
                warehouseService
        );
        this.resetPasswordCommandService = new ResetPasswordCommandService(
                accountRepository,
                passwordService,
                mailService
        );
        this.deactivateUserCommandService = new DeactivateUserCommandService(accountRepository);
        this.reactivateUserCommandService = new ReactivateUserCommandService(accountRepository);
        this.createDirectUserCommandService = new CreateDirectUserCommandService(
                accountRepository,
                roleRepository,
                passwordService,
                warehouseService
        );
        this.createCompanyCommandService = new CreateCompanyCommandService(
                accountRepository,
                tenantRepository,
                roleRepository,
                memberTokenRepository,
                tokenService,
                mailService
        );
        this.updateCompanyCommandService = new UpdateCompanyCommandService(tenantRepository);
        this.createAdminUserCommandService = new CreateAdminUserCommandService(
                accountRepository,
                invitationRepository,
                roleRepository,
                passwordService,
                mailService
        );
        this.updateAdminUserCommandService = new UpdateAdminUserCommandService(accountRepository);
        this.createSellerCommandService = new CreateSellerCommandService(
                sellerRepository,
                sellerWarehouseRepository,
                warehouseService
        );
        this.updateRolePermissionsCommandService = new UpdateRolePermissionsCommandService(
                roleRepository,
                rolePermissionRepository,
                rolePermissionHistoryRepository
        );
    }

    MemberResponses.LoginResponse login(MemberRequests.LoginRequest request) {
        MemberResponses.LoginResponse response = new MemberResponses.LoginResponse();
        copy(loginCommandService.login(request), response);
        return response;
    }

    MemberResponses.SetupPasswordResponse setupPassword(MemberRequests.SetupPasswordRequest request) {
        MemberResponses.SetupPasswordResponse response = new MemberResponses.SetupPasswordResponse();
        copy(setupPasswordCommandService.setupPassword(request), response);
        return response;
    }

    MemberResponses.InviteAccountResponse invite(MemberRequests.InviteAccountRequest request, String inviterAccountId) {
        MemberResponses.InviteAccountResponse response = new MemberResponses.InviteAccountResponse();
        copy(inviteAccountCommandService.invite(request, inviterAccountId), response);
        return response;
    }

    MemberResponses.SimpleUserStatusResponse resetPassword(String userId) {
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        copy(resetPasswordCommandService.resetPassword(userId), response);
        return response;
    }

    MemberResponses.SimpleUserStatusResponse deactivate(String userId) {
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        copy(deactivateUserCommandService.deactivate(userId), response);
        return response;
    }

    MemberResponses.SimpleUserStatusResponse reactivate(String userId) {
        MemberResponses.SimpleUserStatusResponse response = new MemberResponses.SimpleUserStatusResponse();
        copy(reactivateUserCommandService.reactivate(userId), response);
        return response;
    }

    MemberResponses.CreateDirectUserResponse createDirect(MemberRequests.CreateDirectUserRequest request) {
        MemberResponses.CreateDirectUserResponse response = new MemberResponses.CreateDirectUserResponse();
        copy(createDirectUserCommandService.createDirect(request), response);
        return response;
    }

    MemberResponses.CreateCompanyResponse createCompany(MemberRequests.CreateCompanyRequest request) {
        MemberResponses.CreateCompanyResponse response = new MemberResponses.CreateCompanyResponse();
        copy(createCompanyCommandService.createCompany(request), response);
        return response;
    }

    MemberResponses.UpdateCompanyResponse updateCompany(String id, MemberRequests.UpdateCompanyRequest request) {
        MemberResponses.UpdateCompanyResponse response = new MemberResponses.UpdateCompanyResponse();
        copy(updateCompanyCommandService.updateCompany(id, request), response);
        return response;
    }

    MemberResponses.CreateAdminUserResponse createAdminUser(MemberRequests.CreateAdminUserRequest request) {
        MemberResponses.CreateAdminUserResponse response = new MemberResponses.CreateAdminUserResponse();
        copy(createAdminUserCommandService.createAdminUser(request), response);
        return response;
    }

    MemberResponses.UpdateAdminUserResponse updateAdminUser(String id, MemberRequests.UpdateAdminUserRequest request) {
        MemberResponses.UpdateAdminUserResponse response = new MemberResponses.UpdateAdminUserResponse();
        copy(updateAdminUserCommandService.updateAdminUser(id, request), response);
        return response;
    }

    MemberResponses.CreateSellerResponse createSeller(MemberRequests.CreateSellerRequest request) {
        MemberResponses.CreateSellerResponse response = new MemberResponses.CreateSellerResponse();
        copy(createSellerCommandService.createSeller(request), response);
        return response;
    }

    MemberResponses.RolePermissionUpdateResponse updateRolePermissions(String roleId, MemberRequests.UpdateRolePermissionsRequest request, String changedBy) {
        MemberResponses.RolePermissionUpdateResponse response = new MemberResponses.RolePermissionUpdateResponse();
        copy(updateRolePermissionsCommandService.updateRolePermissions(roleId, request, changedBy), response);
        return response;
    }

    private static void copy(com.conk.member.command.application.dto.response.LoginResponse source, MemberResponses.LoginResponse target) {
        target.setToken(source.getToken()); target.setRefreshToken(source.getRefreshToken()); target.setId(source.getId()); target.setEmail(source.getEmail()); target.setName(source.getName()); target.setRole(source.getRole()); target.setStatus(source.getStatus()); target.setTenantId(source.getTenantId()); target.setTenantName(source.getTenantName()); target.setSellerId(source.getSellerId()); target.setWarehouseId(source.getWarehouseId());
    }
    private static void copy(com.conk.member.command.application.dto.response.SetupPasswordResponse source, MemberResponses.SetupPasswordResponse target) {
        target.setAccountId(source.getAccountId()); target.setAccountStatus(source.getAccountStatus()); target.setPasswordChangedAt(source.getPasswordChangedAt()); target.setTenantStatus(source.getTenantStatus()); target.setActivatedAt(source.getActivatedAt());
    }
    private static void copy(com.conk.member.command.application.dto.response.InviteAccountResponse source, MemberResponses.InviteAccountResponse target) {
        target.setInvitationId(source.getInvitationId()); target.setRole(source.getRole()); target.setTenantId(source.getTenantId()); target.setSellerId(source.getSellerId()); target.setWarehouseId(source.getWarehouseId()); target.setName(source.getName()); target.setEmail(source.getEmail()); target.setInviteStatus(source.getInviteStatus()); target.setInviteSentAt(source.getInviteSentAt());
    }
    private static void copy(com.conk.member.command.application.dto.response.SimpleUserStatusResponse source, MemberResponses.SimpleUserStatusResponse target) {
        target.setAccountStatus(source.getAccountStatus()); target.setIsTemporaryPassword(source.getIsTemporaryPassword());
    }
    private static void copy(com.conk.member.command.application.dto.response.CreateDirectUserResponse source, MemberResponses.CreateDirectUserResponse target) {
        target.setId(source.getId()); target.setRole(source.getRole()); target.setName(source.getName()); target.setWorkerCode(source.getWorkerCode()); target.setTenantId(source.getTenantId()); target.setWarehouseId(source.getWarehouseId()); target.setAccountStatus(source.getAccountStatus());
    }
    private static void copy(com.conk.member.command.application.dto.response.CreateCompanyResponse source, MemberResponses.CreateCompanyResponse target) {
        target.setId(source.getId()); target.setTenantCode(source.getTenantCode()); target.setName(source.getName()); target.setStatus(source.getStatus()); target.setCreatedAt(source.getCreatedAt()); target.setMasterAdminUserId(source.getMasterAdminUserId()); target.setMasterAdminEmail(source.getMasterAdminEmail());
    }
    private static void copy(com.conk.member.command.application.dto.response.UpdateCompanyResponse source, MemberResponses.UpdateCompanyResponse target) {
        target.setId(source.getId()); target.setTenantCode(source.getTenantCode()); target.setName(source.getName()); target.setStatus(source.getStatus()); target.setUpdatedAt(source.getUpdatedAt());
    }
    private static void copy(com.conk.member.command.application.dto.response.CreateAdminUserResponse source, MemberResponses.CreateAdminUserResponse target) {
        target.setId(source.getId()); target.setTenantId(source.getTenantId()); target.setName(source.getName()); target.setEmail(source.getEmail()); target.setRole(source.getRole()); target.setStatus(source.getStatus()); target.setInvitationId(source.getInvitationId());
    }
    private static void copy(com.conk.member.command.application.dto.response.UpdateAdminUserResponse source, MemberResponses.UpdateAdminUserResponse target) {
        target.setId(source.getId()); target.setTenantId(source.getTenantId()); target.setName(source.getName()); target.setEmail(source.getEmail()); target.setRole(source.getRole()); target.setStatus(source.getStatus()); target.setUpdatedAt(source.getUpdatedAt());
    }
    private static void copy(com.conk.member.command.application.dto.response.CreateSellerResponse source, MemberResponses.CreateSellerResponse target) {
        target.setId(source.getId()); target.setCustomerCode(source.getCustomerCode()); target.setBrandNameKo(source.getBrandNameKo()); target.setStatus(source.getStatus()); target.setCreatedAt(source.getCreatedAt());
    }
    private static void copy(com.conk.member.command.application.dto.response.RolePermissionUpdateResponse source, MemberResponses.RolePermissionUpdateResponse target) {
        target.setRoleId(source.getRoleId()); target.setUpdatedPermissionCount(source.getUpdatedPermissionCount()); target.setChangedAt(source.getChangedAt()); target.setChangedBy(source.getChangedBy());
    }
}
