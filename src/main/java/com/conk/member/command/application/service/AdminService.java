package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.request.CreateCompanyLogRequest;
import com.conk.member.command.application.dto.request.CreateCompanyRequest;
import com.conk.member.command.application.dto.request.UpdateAdminUserRequest;
import com.conk.member.command.application.dto.request.UpdateCompanyRequest;
import com.conk.member.command.application.dto.response.CompanyLogResponse;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.application.dto.response.CreateCompanyResponse;
import com.conk.member.command.application.dto.response.UpdateAdminUserResponse;
import com.conk.member.command.application.dto.response.UpdateCompanyResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.aggregate.TenantLog;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.enums.TokenType;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantLogRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminService {

    private final AccountRepository accountRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final TenantLogRepository tenantLogRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final MailService mailService;

    public AdminService(AccountRepository accountRepository,
                        InvitationRepository invitationRepository,
                        RoleRepository roleRepository,
                        TenantRepository tenantRepository,
                        TenantLogRepository tenantLogRepository,
                        MemberTokenRepository memberTokenRepository,
                        PasswordService passwordService,
                        TokenService tokenService,
                        MailService mailService) {
        this.accountRepository = accountRepository;
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.tenantLogRepository = tenantLogRepository;
        this.memberTokenRepository = memberTokenRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    // ─── createAdminUser ──────────────────────────────────────────────────────

    public CreateAdminUserResponse createAdminUser(CreateAdminUserRequest request) {
        if (!RoleName.MASTER_ADMIN.name().equals(request.getRole())) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "MASTER_ADMIN 추가 발급만 허용합니다.");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

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

        String companyName = tenantRepository.findById(request.getTenantId())
                .map(Tenant::getTenantName).orElse("");
        mailService.sendInviteMail(request.getEmail(), request.getName(),
                RoleName.MASTER_ADMIN.name(), companyName, temporaryPassword);

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

    // ─── updateAdminUser ──────────────────────────────────────────────────────

    public UpdateAdminUserResponse updateAdminUser(String id, UpdateAdminUserRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(account.getEmail())
                && accountRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (StringUtils.hasText(request.getStatus())) {
            validateLastActiveMasterAdmin(account, request.getStatus());
            account.setAccountStatus(AccountStatus.valueOf(request.getStatus()));
        }
        if (StringUtils.hasText(request.getName())) account.setAccountName(request.getName());
        if (StringUtils.hasText(request.getEmail())) account.setEmail(request.getEmail());

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

    // ─── createCompany ────────────────────────────────────────────────────────

    public CreateCompanyResponse createCompany(CreateCompanyRequest request) {
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

        CreateCompanyResponse response = new CreateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setCreatedAt(tenant.getCreatedAt());
        return response;
    }

    // ─── updateCompany ────────────────────────────────────────────────────────

    public UpdateCompanyResponse updateCompany(String id, UpdateCompanyRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (StringUtils.hasText(request.getTenantName())) tenant.setTenantName(request.getTenantName());
        if (StringUtils.hasText(request.getRepresentativeName())) tenant.setRepresentativeName(request.getRepresentativeName());
        if (StringUtils.hasText(request.getBusinessNo())) tenant.setBusinessNo(request.getBusinessNo());
        if (StringUtils.hasText(request.getPhoneNo())) tenant.setPhoneNo(request.getPhoneNo());
        if (StringUtils.hasText(request.getEmail())) tenant.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getAddress())) tenant.setAddress(request.getAddress());
        if (StringUtils.hasText(request.getTenantType())) tenant.setTenantType(request.getTenantType());
        if (StringUtils.hasText(request.getStatus())) tenant.setStatus(TenantStatus.valueOf(request.getStatus()));

        tenantRepository.save(tenant);

        UpdateCompanyResponse response = new UpdateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setUpdatedAt(tenant.getUpdatedAt());
        return response;
    }

    // ─── createCompanyLog / getCompanyLogs ─────────────────────────────────────

    public CompanyLogResponse createCompanyLog(CreateCompanyLogRequest request, String actorId) {
        TenantLog log = new TenantLog();
        log.setLogId(generateId("LOG"));
        log.setTenantId(request.getCompanyId());
        log.setActor(actorId);                    // principal에서 추출한 실제 요청자
        log.setAction(request.getAction());
        log.setLoggedAt(LocalDateTime.now());     // FE at 값 무시, 서버 시각 사용
        tenantLogRepository.save(log);
        return toCompanyLogResponse(log);
    }

    public List<CompanyLogResponse> getCompanyLogs(String companyId) {
        return tenantLogRepository.findTop10ByTenantIdOrderByLoggedAtDesc(companyId)
                .stream().map(this::toCompanyLogResponse).toList();
    }

    private CompanyLogResponse toCompanyLogResponse(TenantLog log) {
        CompanyLogResponse response = new CompanyLogResponse();
        response.setLogId(log.getLogId());
        response.setCompanyId(log.getTenantId());
        response.setAt(log.getLoggedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.setActor(log.getActor());
        response.setAction(log.getAction());
        return response;
    }

    // ─── private helpers ──────────────────────────────────────────────────────

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

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
