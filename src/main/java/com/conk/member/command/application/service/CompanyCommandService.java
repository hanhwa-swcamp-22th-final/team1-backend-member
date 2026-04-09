package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateCompanyRequest;
import com.conk.member.command.application.dto.request.UpdateCompanyRequest;
import com.conk.member.command.application.dto.response.CreateCompanyResponse;
import com.conk.member.command.application.dto.response.UpdateCompanyResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.enums.TokenType;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CompanyCommandService {

    private final TenantRepository tenantRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final TokenService tokenService;
    private final MailService mailService;

    public CompanyCommandService(TenantRepository tenantRepository,
                                 AccountRepository accountRepository,
                                 RoleRepository roleRepository,
                                 MemberTokenRepository memberTokenRepository,
                                 TokenService tokenService,
                                 MailService mailService) {
        this.tenantRepository = tenantRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.memberTokenRepository = memberTokenRepository;
        this.tokenService = tokenService;
        this.mailService = mailService;
    }

    public CreateCompanyResponse createCompany(CreateCompanyRequest request) {
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
        mailService.sendSetupLink(adminAccount.getEmail(), adminAccount.getAccountName(), tenant.getTenantName(), rawSetupToken);

        CreateCompanyResponse response = new CreateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setMasterAdminUserId(adminAccount.getAccountId());
        response.setMasterAdminEmail(adminAccount.getEmail());
        return response;
    }

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

    private void validateDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
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
