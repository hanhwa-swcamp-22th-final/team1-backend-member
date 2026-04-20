package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.request.ChangePasswordRequest;
import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.ChangePasswordResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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

    // ─── login ────────────────────────────────────────────────────────────────

    public LoginResponse login(LoginRequest request) {
        log.info("[LOGIN] 시도: identifier={}", request.getEmail());
        Account account = findLoginAccount(request.getEmail());
        log.info("[LOGIN] 계정 조회 성공: accountId={}, status={}", account.getAccountId(), account.getAccountStatus());
        validatePassword(request.getPassword(), account.getPasswordHash());
        log.info("[LOGIN] 비밀번호 검증 성공: accountId={}", account.getAccountId());
        validateAccountIsActive(account);
        log.info("[LOGIN] 계정 활성 확인 완료: accountId={}", account.getAccountId());

        account.successLogin();
        accountRepository.save(account);

        String accessToken = jwtTokenProvider.createToken(account);
        String refreshToken = jwtTokenProvider.createRefreshToken(account);
        saveRefreshToken(account.getAccountId(), refreshToken);

        return buildLoginResponse(account, accessToken, refreshToken);
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    public void logout(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "로그아웃할 사용자 정보를 확인할 수 없습니다.");
        }
        refreshTokenRepository.deleteById(accountId);
    }

    // ─── refresh ──────────────────────────────────────────────────────────────

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

    // ─── invite ───────────────────────────────────────────────────────────────

    public InviteAccountResponse invite(InviteAccountRequest request,
                                        String inviterAccountId,
                                        String inviterTenantId) {
        RoleName roleName = parseRoleName(request.getRole());
        if (!StringUtils.hasText(inviterAccountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "초대 요청자를 확인할 수 없습니다.");
        }
        validateInviteRole(roleName);
        resolveOrganizationId(roleName, request);    // organizationId → sellerId/warehouseId 분기
        if (roleName == RoleName.WH_WORKER) {
            validateDuplicateWorkerCode(request.getEmployeeNumber());
        } else {
            validateDuplicateEmail(request.getEmail());
        }
        validateInviteReference(roleName, request);

        Role role = getRole(roleName);
        String temporaryPassword = passwordService.generateTemporaryPassword();

        Account invitedAccount = new Account();
        invitedAccount.setAccountId(generateId("ACC"));
        invitedAccount.setRole(role);
        invitedAccount.setTenantId(inviterTenantId);             // FE는 tenantId 미전송 → 초대자 tenantId 사용
        invitedAccount.setSellerId(request.getSellerId());
        invitedAccount.setWarehouseId(request.getWarehouseId());
        invitedAccount.setAccountName(request.getName());
        if (roleName == RoleName.WH_WORKER) {
            // 워커: 사번을 workerCode로 저장, 초기 비밀번호 = 사번
            invitedAccount.setWorkerCode(request.getEmployeeNumber());
            invitedAccount.applyTemporaryPassword(passwordService.encode(request.getEmployeeNumber()));
        } else {
            invitedAccount.setEmail(request.getEmail());
            invitedAccount.applyTemporaryPassword(passwordService.encode(temporaryPassword));
        }
        accountRepository.save(invitedAccount);

        Invitation invitation = new Invitation();
        invitation.setInvitationId(generateId("INV"));
        invitation.setInviterAccountId(inviterAccountId);
        invitation.setInviteeAccountId(invitedAccount.getAccountId());
        invitation.setTargetRoleId(role.getRoleId());
        invitation.setTenantId(inviterTenantId);                 // FE는 tenantId 미전송 → 초대자 tenantId 사용
        invitation.setSellerId(request.getSellerId());
        invitation.setWarehouseId(request.getWarehouseId());
        invitation.setInviteEmail(resolveInviteRecipient(roleName, request));
        invitation.markPending();
        invitationRepository.save(invitation);

        if (roleName != RoleName.WH_WORKER) {
            // 워커는 이메일 없으므로 초대 메일 발송 생략
            String companyName = resolveCompanyName(inviterTenantId);
            mailService.sendInviteMail(request.getEmail(), request.getName(),
                    roleName.name(), companyName, temporaryPassword);
        }

        return buildInviteResponse(invitedAccount, invitation, roleName.name());
    }

    // ─── setupPassword ────────────────────────────────────────────────────────

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

    public ChangePasswordResponse changePassword(String accountId, ChangePasswordRequest request) {
        if (!StringUtils.hasText(accountId)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED);
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (account.getAccountStatus() != AccountStatus.TEMP_PASSWORD) {
            throw new MemberException(ErrorCode.FORBIDDEN, "임시 비밀번호 상태의 계정만 비밀번호를 변경할 수 있습니다.");
        }

        account.changePassword(passwordService.encode(request.getNewPassword()));
        accountRepository.save(account);

        return buildChangePasswordResponse(account);
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    private Account findLoginAccount(String emailOrWorkerCode) {
        return accountRepository.findByEmail(emailOrWorkerCode)
                .or(() -> accountRepository.findByWorkerCode(emailOrWorkerCode))
                .orElseThrow(() -> {
                    log.warn("[LOGIN] 계정 없음: identifier={}", emailOrWorkerCode);
                    return new MemberException(ErrorCode.INVALID_CREDENTIALS);
                });
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordService.matches(rawPassword, encodedPassword)) {
            log.warn("[LOGIN] 비밀번호 불일치");
            throw new MemberException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void validateAccountIsActive(Account account) {
        if (account.getAccountStatus() == AccountStatus.INACTIVE) {
            log.warn("[LOGIN] 비활성 계정: accountId={}", account.getAccountId());
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
        if (roleName != RoleName.WH_MANAGER && roleName != RoleName.WH_WORKER && roleName != RoleName.SELLER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED, "WH_MANAGER/WH_WORKER/SELLER 초대만 허용합니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
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
        userInfo.setWorkerCode(account.getWorkerCode());
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
        response.setEmail(account.getEmail());
        response.setWorkerCode(account.getWorkerCode());     // WH_WORKER 이외 역할은 null
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

    private ChangePasswordResponse buildChangePasswordResponse(Account account) {
        ChangePasswordResponse response = new ChangePasswordResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountStatus(account.getAccountStatus().name());
        response.setPasswordChangedAt(account.getPasswordChangedAt());
        return response;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ─── organizationId 분기 ──────────────────────────────────────────────────

    /**
     * FE는 역할에 관계없이 단일 필드 organizationId로 조직 ID를 전송합니다.
     * 역할에 따라 sellerId 또는 warehouseId에 매핑합니다.
     */
    private void resolveOrganizationId(RoleName roleName, InviteAccountRequest request) {
        String orgId = request.getOrganizationId();
        if (!StringUtils.hasText(orgId)) return;
        if (roleName == RoleName.SELLER) {
            request.setSellerId(orgId);
        } else if (roleName == RoleName.WH_MANAGER || roleName == RoleName.WH_WORKER) {
            request.setWarehouseId(orgId);
        }
    }

    /**
     * WH_WORKER 사번(employeeNumber) 중복 검증.
     * workerCode는 로그인 ID 역할을 하므로 중복 불허.
     */
    private void validateDuplicateWorkerCode(String workerCode) {
        if (!StringUtils.hasText(workerCode)) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "사번(employeeNumber)은 필수입니다.");
        }
        if (accountRepository.existsByWorkerCode(workerCode)) {
            throw new MemberException(ErrorCode.DUPLICATE_WORKER_CODE, "이미 사용 중인 사번입니다.");
        }
    }

    private String resolveInviteRecipient(RoleName roleName, InviteAccountRequest request) {
        if (roleName == RoleName.WH_WORKER) {
            return request.getEmployeeNumber();
        }
        return request.getEmail();
    }
}
