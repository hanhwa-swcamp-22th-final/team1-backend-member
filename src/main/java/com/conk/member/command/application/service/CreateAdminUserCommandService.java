package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.command.infrastructure.mail.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateAdminUserCommandService {

    private final AccountRepository accountRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordService passwordService;
    private final MailService mailService;

    public CreateAdminUserCommandService(AccountRepository accountRepository,
                                         InvitationRepository invitationRepository,
                                         RoleRepository roleRepository,
                                         TenantRepository tenantRepository,
                                         PasswordService passwordService,
                                         MailService mailService) {
        this.accountRepository = accountRepository;
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordService = passwordService;
        this.mailService = mailService;
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

        String companyName = resolveCompanyName(request.getTenantId());
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

    private void validateDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String resolveCompanyName(String tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::getTenantName)
                .orElse("");
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
