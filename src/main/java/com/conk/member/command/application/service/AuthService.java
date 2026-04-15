package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.aggregate.RefreshToken;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final AccountRepository accountRepository;
    private final SellerRepository sellerRepository;
    private final InvitationRepository invitationRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MailService mailService;
    private final WarehouseService warehouseService;

    public AuthService(AccountRepository accountRepository,
                       SellerRepository sellerRepository,
                       InvitationRepository invitationRepository,
                       RoleRepository roleRepository,
                       TenantRepository tenantRepository,
                       MemberTokenRepository memberTokenRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordService passwordService,
                       TokenService tokenService,
                       JwtTokenProvider jwtTokenProvider,
                       MailService mailService,
                       WarehouseService warehouseService) {
        this.accountRepository = accountRepository;
        this.sellerRepository = sellerRepository;
        this.invitationRepository = invitationRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.memberTokenRepository = memberTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mailService = mailService;
        this.warehouseService = warehouseService;
    }

    public long getRefreshExpiration() {
        return jwtTokenProvider.getRefreshExpiration();
    }

    public LoginResponse login(LoginRequest request) {
        Account account = findLoginAccount(request.getEmailOrWorkerCode());
        validatePassword(request.getPassword(), account.getPasswordHash());
        validateAccountIsActive(account);

        account.successLogin();
        accountRepository.save(account);

        String accessToken = jwtTokenProvider.createToken(account);
        String refreshToken = jwtTokenProvider.createRefreshToken(account);
        saveRefreshToken(account.getAccountId(), refreshToken);

        return buildLoginResponse(account, accessToken, refreshToken);
    }

    public void logout(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "로그아웃할 사용자 정보를 확인할 수 없습니다.");
        }
        refreshTokenRepository.deleteById(accountId);
    }

    public LoginResponse refreshToken(String providedRefreshToken) {
        jwtTokenProvider.validateRefreshToken(providedRefreshToken);
        String accountId = jwtTokenProvider.getAccountIdFromJWT(providedRefreshToken);
        RefreshToken stored = getStoredRefreshToken(accountId);
        validateStoredRefreshToken(stored, providedRefreshToken);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createToken(account);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(account);
        saveRefreshToken(account.getAccountId(), newRefreshToken);

        return buildLoginResponse(account, newAccessToken, newRefreshToken);
    }

    public InviteAccountResponse invite(InviteAccountRequest request, String inviterAccountId) {
        RoleName roleName = parseRoleName(request.getRole());
        if (!StringUtils.hasText(inviterAccountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "초대 요청자를 확인할 수 없습니다.");
        }

        request.setTenantId(resolveTenantIdForInvite(request, inviterAccountId));
        validateInviteRole(roleName);
        validateInviteReference(roleName, request);

        if (roleName.isWarehouseWorker()) {
            return createWorkerAccountForFrontend(request);
        }

        validateDuplicateEmail(request.getEmail());

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

        String companyName = resolveCompanyName(request.getTenantId());
        mailService.sendInviteMail(request.getEmail(), request.getName(),
                roleName.name(), companyName, temporaryPassword);

        return buildInviteResponse(invitedAccount, invitation, roleName.name());
    }

    public SetupPasswordResponse setupPassword(SetupPasswordRequest request) {
        String tokenHash = tokenService.hash(request.getSetupToken());
        MemberToken memberToken = memberTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new MemberException(ErrorCode.UNAUTHORIZED));

        validateSetupToken(memberToken);

        Account account = accountRepository.findById(memberToken.getAccountId())
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        account.changePassword(passwordService.encode(request.getNewPassword()));
        accountRepository.save(account);

        memberToken.use();
        memberTokenRepository.save(memberToken);

        return buildSetupPasswordResponse(account);
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

    private RefreshToken getStoredRefreshToken(String accountId) {
        return refreshTokenRepository.findById(accountId)
                .orElseThrow(() -> new BadCredentialsException("Refresh Token을 찾을 수 없습니다."));
    }

    private void validateStoredRefreshToken(RefreshToken stored, String provided) {
        if (!stored.getToken().equals(provided)) {
            throw new BadCredentialsException("Refresh Token이 일치하지 않습니다.");
        }
        if (stored.getExpiryDate().before(new Date())) {
            throw new BadCredentialsException("Refresh Token이 만료되었습니다.");
        }
    }

    private void validateInviteRole(RoleName roleName) {
        if (roleName != RoleName.WH_MANAGER && roleName != RoleName.SELLER && roleName != RoleName.WH_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED, "WH_MANAGER/WH_WORKER/SELLER 처리만 허용합니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "email은 필수입니다.");
        }
        if (accountRepository.existsByEmail(email)) {
            throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateInviteReference(RoleName roleName, InviteAccountRequest request) {
        if ((roleName == RoleName.WH_MANAGER || roleName == RoleName.WH_WORKER)
                && !warehouseService.exists(request.getWarehouseId())) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
        }
        if (roleName == RoleName.SELLER && sellerRepository.findById(request.getSellerId()).isEmpty()) {
            throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 셀러입니다.");
        }
    }

    private String resolveTenantIdForInvite(InviteAccountRequest request, String inviterAccountId) {
        if (StringUtils.hasText(request.getTenantId())) {
            return request.getTenantId();
        }
        return accountRepository.findById(inviterAccountId)
                .map(Account::getTenantId)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new MemberException(ErrorCode.BAD_REQUEST, "tenantId를 확인할 수 없습니다."));
    }

    private InviteAccountResponse createWorkerAccountForFrontend(InviteAccountRequest request) {
        if (!StringUtils.hasText(request.getEmployeeNumber())) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "employeeNumber는 필수입니다.");
        }
        if (accountRepository.existsByWorkerCode(request.getEmployeeNumber())) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE);
        }

        Role role = getRole(RoleName.WH_WORKER);
        Account account = new Account();
        account.setAccountId(generateId("ACC"));
        account.setRole(role);
        account.setTenantId(request.getTenantId());
        account.setWarehouseId(request.getWarehouseId());
        account.setAccountName(request.getName());
        account.setWorkerCode(request.getEmployeeNumber());
        if (StringUtils.hasText(request.getEmail())) {
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new MemberException(ErrorCode.DUPLICATE_EMAIL);
            }
            account.setEmail(request.getEmail());
        }
        account.setPasswordHash(passwordService.encode(request.getEmployeeNumber()));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);

        InviteAccountResponse response = new InviteAccountResponse();
        response.setInvitationId(null);
        response.setRole(RoleName.WH_WORKER.name());
        response.setTenantId(account.getTenantId());
        response.setWarehouseId(account.getWarehouseId());
        response.setName(account.getAccountName());
        response.setEmail(account.getEmail());
        response.setInviteStatus(AccountStatus.ACTIVE.name());
        response.setInviteSentAt(LocalDateTime.now());
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

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
    }

    private String resolveCompanyName(String tenantId) {
        if (!StringUtils.hasText(tenantId)) return "";
        return tenantRepository.findById(tenantId).map(Tenant::getTenantName).orElse("");
    }

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.fromValue(roleName);
        } catch (IllegalArgumentException e) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "유효하지 않은 역할입니다.");
        }
    }

    private LoginResponse buildLoginResponse(Account account, String accessToken, String refreshToken) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(account.getAccountId());
        userInfo.setEmail(account.getEmail());
        userInfo.setName(account.getAccountName());
        userInfo.setRole(account.getRole().getRoleName().name());
        userInfo.setStatus(account.getAccountStatus().name());
        userInfo.setTenantId(account.getTenantId());
        userInfo.setSellerId(account.getSellerId());
        userInfo.setWarehouseId(account.getWarehouseId());
        if (StringUtils.hasText(account.getTenantId())) {
            tenantRepository.findById(account.getTenantId())
                    .ifPresent(tenant -> userInfo.setOrganization(tenant.getTenantName()));
        }

        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(userInfo);
        return response;
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

    private SetupPasswordResponse buildSetupPasswordResponse(Account account) {
        SetupPasswordResponse response = new SetupPasswordResponse();
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

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
