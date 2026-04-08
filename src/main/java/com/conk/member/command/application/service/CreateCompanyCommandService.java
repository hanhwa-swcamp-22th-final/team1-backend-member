package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateCompanyRequest;
import com.conk.member.command.application.dto.response.CreateCompanyResponse;
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
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CreateCompanyCommandService {

    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final TokenService tokenService;
    private final MailService mailService;

    public CreateCompanyCommandService(AccountRepository accountRepository,
                                       TenantRepository tenantRepository,
                                       RoleRepository roleRepository,
                                       MemberTokenRepository memberTokenRepository,
                                       TokenService tokenService,
                                       MailService mailService) {
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
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
        mailService.sendSetupLink(adminAccount.getEmail(), rawSetupToken);

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
