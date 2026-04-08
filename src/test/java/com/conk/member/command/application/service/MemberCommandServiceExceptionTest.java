package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.*;
import com.conk.member.command.infrastructure.service.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.common.jwt.JwtTokenProvider;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceExceptionTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private InvitationRepository invitationRepository;
    @Mock private MemberTokenRepository memberTokenRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private RolePermissionHistoryRepository rolePermissionHistoryRepository;
    @Mock private PasswordService passwordService;
    @Mock private TokenService tokenService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private MailService mailService;
    @Mock private WarehouseService warehouseService;

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("초대는 WAREHOUSE_MANAGER 또는 SELLER만 허용한다")
    void invite_fail_when_role_out_of_scope() {
        MemberRequests.InviteAccountRequest request = new MemberRequests.InviteAccountRequest();
        request.setRole(RoleName.MASTER_ADMIN.name());
        request.setEmail("master@conk.com");

        assertThatThrownBy(() -> memberCommandService.invite(request, "ACC-001"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.BAD_REQUEST);

        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("중복 이메일은 초대할 수 없다")
    void invite_fail_when_email_duplicate() {
        MemberRequests.InviteAccountRequest request = new MemberRequests.InviteAccountRequest();
        request.setRole(RoleName.WAREHOUSE_MANAGER.name());
        request.setEmail("dup@conk.com");

        when(accountRepository.existsByEmail("dup@conk.com")).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.invite(request, "ACC-001"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(invitationRepository, never()).save(any());
        verify(mailService, never()).sendTemporaryPassword(any(), any());
    }

    @Test
    @DisplayName("창고관리자 초대는 유효한 창고가 없으면 실패한다")
    void invite_fail_when_warehouse_invalid() {
        MemberRequests.InviteAccountRequest request = new MemberRequests.InviteAccountRequest();
        request.setRole(RoleName.WAREHOUSE_MANAGER.name());
        request.setWarehouseId("WH-404");
        request.setEmail("wm@conk.com");

        when(accountRepository.existsByEmail("wm@conk.com")).thenReturn(false);
        when(warehouseService.exists("WH-404")).thenReturn(false);

        assertThatThrownBy(() -> memberCommandService.invite(request, "ACC-001"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REFERENCE);
    }

    @Test
    @DisplayName("셀러 담당자 초대는 유효한 셀러가 없으면 실패한다")
    void invite_fail_when_seller_invalid() {
        MemberRequests.InviteAccountRequest request = new MemberRequests.InviteAccountRequest();
        request.setRole(RoleName.SELLER.name());
        request.setSellerId("SELLER-404");
        request.setEmail("seller@conk.com");

        when(accountRepository.existsByEmail("seller@conk.com")).thenReturn(false);
        when(sellerRepository.findById("SELLER-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberCommandService.invite(request, "ACC-001"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REFERENCE);
    }

    @Test
    @DisplayName("직접 발급은 중복 이메일이면 실패한다")
    void create_direct_fail_when_email_duplicate() {
        MemberRequests.CreateDirectUserRequest request = new MemberRequests.CreateDirectUserRequest();
        request.setWorkerCode("WORKER-001");
        request.setEmail("worker@conk.com");

        when(accountRepository.existsByWorkerCode("WORKER-001")).thenReturn(false);
        when(accountRepository.existsByEmail("worker@conk.com")).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.createDirect(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("직접 발급은 유효하지 않은 창고면 실패한다")
    void create_direct_fail_when_warehouse_invalid() {
        MemberRequests.CreateDirectUserRequest request = new MemberRequests.CreateDirectUserRequest();
        request.setWorkerCode("WORKER-001");
        request.setWarehouseId("WH-404");

        when(accountRepository.existsByWorkerCode("WORKER-001")).thenReturn(false);
        when(warehouseService.exists("WH-404")).thenReturn(false);

        assertThatThrownBy(() -> memberCommandService.createDirect(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REFERENCE);
    }

    @Test
    @DisplayName("업체 등록은 최초 총괄관리자 이메일이 중복이면 실패한다")
    void create_company_fail_when_master_email_duplicate() {
        MemberRequests.CreateCompanyRequest request = new MemberRequests.CreateCompanyRequest();
        request.setMasterAdminEmail("dup@conk.com");

        when(accountRepository.existsByEmail("dup@conk.com")).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.createCompany(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("업체 등록은 MASTER_ADMIN 역할이 없으면 실패한다")
    void create_company_fail_when_master_role_missing() {
        MemberRequests.CreateCompanyRequest request = new MemberRequests.CreateCompanyRequest();
        request.setTenantName("FASTSHIP");
        request.setMasterAdminEmail("master@conk.com");

        when(accountRepository.existsByEmail("master@conk.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.MASTER_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberCommandService.createCompany(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("추가 총괄관리자 발급은 중복 이메일이면 실패한다")
    void create_admin_user_fail_when_email_duplicate() {
        MemberRequests.CreateAdminUserRequest request = new MemberRequests.CreateAdminUserRequest();
        request.setRole(RoleName.MASTER_ADMIN.name());
        request.setEmail("dup@conk.com");

        when(accountRepository.existsByEmail("dup@conk.com")).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.createAdminUser(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("관리자 수정은 변경 이메일이 이미 사용 중이면 실패한다")
    void update_admin_user_fail_when_email_duplicate() {
        Account account = account("ACC-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
        account.setTenantId("TENANT-001");
        account.setEmail("before@conk.com");

        MemberRequests.UpdateAdminUserRequest request = new MemberRequests.UpdateAdminUserRequest();
        request.setEmail("after@conk.com");

        when(accountRepository.findById("ACC-001")).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmail("after@conk.com")).thenReturn(true);

        assertThatThrownBy(() -> memberCommandService.updateAdminUser("ACC-001", request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("비밀번호 초기화는 대상 계정이 없으면 실패한다")
    void reset_password_fail_when_user_not_found() {
        when(accountRepository.findById("ACC-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberCommandService.resetPassword("ACC-404"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("셀러 등록은 유효하지 않은 창고가 포함되면 실패한다")
    void create_seller_fail_when_any_warehouse_invalid() {
        MemberRequests.CreateSellerRequest request = new MemberRequests.CreateSellerRequest();
        request.setWarehouseIds(List.of("WH-001", "WH-404"));

        when(warehouseService.exists("WH-001")).thenReturn(true);
        when(warehouseService.exists("WH-404")).thenReturn(false);

        assertThatThrownBy(() -> memberCommandService.createSeller(request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_REFERENCE);

        verify(sellerRepository, never()).save(any());
    }

    @Test
    @DisplayName("권한 매트릭스 수정은 역할이 없으면 실패한다")
    void update_role_permissions_fail_when_role_not_found() {
        when(roleRepository.findById("ROLE-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberCommandService.updateRolePermissions("ROLE-404", new MemberRequests.UpdateRolePermissionsRequest(), "ACC-001"))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("마지막 활성 총괄관리자는 수정 API로도 비활성화할 수 없다")
    void update_admin_user_fail_when_last_active_master_admin_deactivated() {
        Account account = account("ACC-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);
        account.setTenantId("TENANT-001");

        MemberRequests.UpdateAdminUserRequest request = new MemberRequests.UpdateAdminUserRequest();
        request.setStatus(AccountStatus.INACTIVE.name());

        when(accountRepository.findById("ACC-001")).thenReturn(Optional.of(account));
        when(accountRepository.countByTenantIdAndRoleNameAndAccountStatus("TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

        assertThatThrownBy(() -> memberCommandService.updateAdminUser("ACC-001", request))
            .isInstanceOf(MemberException.class)
            .extracting(ex -> ((MemberException) ex).getErrorCode())
            .isEqualTo(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED);
    }

    private Account account(String accountId, RoleName roleName, AccountStatus status) {
        Role role = new Role();
        role.setRoleId("ROLE-" + roleName.name());
        role.setRoleName(roleName);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole(role);
        account.setAccountStatus(status);
        account.setAccountName("테스트 사용자");
        account.setEmail("test@conk.com");
        return account;
    }
}
