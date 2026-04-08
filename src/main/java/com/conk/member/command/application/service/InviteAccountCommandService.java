package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.Role;
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
public class InviteAccountCommandService {

    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;
    private final MailService mailService;
    private final WarehouseService warehouseService;

    public InviteAccountCommandService(AccountRepository accountRepository,
                                       SellerRepository sellerRepository,
                                       InvitationRepository invitationRepository,
                                       RoleRepository roleRepository,
                                       PasswordService passwordService,
                                       MailService mailService,
                                       WarehouseService warehouseService) {
        this.accountRepository = accountRepository;
        this.sellerRepository = sellerRepository;
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
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
        return createInviteResponse(invitedAccount, invitation, roleName.name());
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

    private void validateDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
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

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private InviteAccountResponse createInviteResponse(Account account, Invitation invitation, String roleName) {
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

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
