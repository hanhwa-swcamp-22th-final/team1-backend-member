package com.conk.member.command.service;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class InviteAccountCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock SellerRepository sellerRepository;
    @Mock InvitationRepository invitationRepository;
    @Mock RoleRepository roleRepository;
    @Mock TenantRepository tenantRepository;
    @Mock PasswordService passwordService;
    @Mock MailService mailService;
    @Mock WarehouseService warehouseService;

    @InjectMocks AuthService authService;

    private Role warehouseManagerRole;
    private Role warehouseWorkerRole;
    private Role sellerRole;
    private InviteAccountRequest warehouseManagerRequest;
    private InviteAccountRequest warehouseWorkerRequest;
    private InviteAccountRequest sellerRequest;

    @BeforeEach
    void setUp() {
        warehouseManagerRole = new Role();
        warehouseManagerRole.setRoleId("ROLE-002");
        warehouseManagerRole.setRoleName(RoleName.WH_MANAGER);

        warehouseWorkerRole = new Role();
        warehouseWorkerRole.setRoleId("ROLE-003");
        warehouseWorkerRole.setRoleName(RoleName.WH_WORKER);

        sellerRole = new Role();
        sellerRole.setRoleId("ROLE-004");
        sellerRole.setRoleName(RoleName.SELLER);

        warehouseManagerRequest = new InviteAccountRequest();
        warehouseManagerRequest.setRole("WH_MANAGER");
        warehouseManagerRequest.setTenantId("TENANT-001");
        warehouseManagerRequest.setWarehouseId("WH-001");
        warehouseManagerRequest.setName("창고관리자");
        warehouseManagerRequest.setEmail("manager@example.com");

        warehouseWorkerRequest = new InviteAccountRequest();
        warehouseWorkerRequest.setRole("WH_WORKER");
        warehouseWorkerRequest.setTenantId("TENANT-001");
        warehouseWorkerRequest.setWarehouseId("WH-001");
        warehouseWorkerRequest.setName("창고작업자");
        warehouseWorkerRequest.setEmployeeNumber("WW-1001");

        sellerRequest = new InviteAccountRequest();
        sellerRequest.setRole("SELLER");
        sellerRequest.setTenantId("TENANT-001");
        sellerRequest.setSellerId("SELLER-001");
        sellerRequest.setName("셀러유저");
        sellerRequest.setEmail("seller@example.com");
    }

    @Test
    @DisplayName("WH_MANAGER 초대 성공")
    void invite_warehouseManager_success() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트업체");

        given(accountRepository.existsByEmail("manager@example.com")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(true);
        given(roleRepository.findByRoleName(RoleName.WH_MANAGER)).willReturn(Optional.of(warehouseManagerRole));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("Temp@1234")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(inv -> inv.getArgument(0));
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));
        willDoNothing().given(mailService).sendInviteMail(any(), any(), any(), any(), any());

        InviteAccountResponse response = authService.invite(warehouseManagerRequest, "ACC-INVITER", "TENANT-001");

        assertThat(response.getRole()).isEqualTo(RoleName.WH_MANAGER.name());
        assertThat(response.getEmail()).isEqualTo("manager@example.com");
        assertThat(response.getName()).isEqualTo("창고관리자");
        then(mailService).should().sendInviteMail(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("SELLER 초대 성공")
    void invite_seller_success() {
        Seller seller = new Seller();
        seller.setSellerId("SELLER-001");

        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("테스트업체");

        given(accountRepository.existsByEmail("seller@example.com")).willReturn(false);
        given(sellerRepository.findById("SELLER-001")).willReturn(Optional.of(seller));
        given(roleRepository.findByRoleName(RoleName.SELLER)).willReturn(Optional.of(sellerRole));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("Temp@1234")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(inv -> inv.getArgument(0));
        given(tenantRepository.findById("TENANT-001")).willReturn(Optional.of(tenant));
        willDoNothing().given(mailService).sendInviteMail(any(), any(), any(), any(), any());

        InviteAccountResponse response = authService.invite(sellerRequest, "ACC-INVITER", "TENANT-001");

        assertThat(response.getRole()).isEqualTo(RoleName.SELLER.name());
        assertThat(response.getEmail()).isEqualTo("seller@example.com");
    }

    @Test
    @DisplayName("초대자 ID 없으면 예외 발생")
    void invite_noInviterId_throwsException() {
        assertThatThrownBy(() -> authService.invite(warehouseManagerRequest, null, "TENANT-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("WH_WORKER 발급 성공")
    void invite_worker_success() {
        given(accountRepository.existsByWorkerCode("WW-1001")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(true);
        given(roleRepository.findByRoleName(RoleName.WH_WORKER)).willReturn(Optional.of(warehouseWorkerRole));
        given(passwordService.generateTemporaryPassword()).willReturn("Temp@1234");
        given(passwordService.encode("WW-1001")).willReturn("$2a$workerEncoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));
        given(invitationRepository.save(any(Invitation.class))).willAnswer(inv -> inv.getArgument(0));

        InviteAccountResponse response = authService.invite(warehouseWorkerRequest, "ACC-INVITER", "TENANT-001");

        assertThat(response.getRole()).isEqualTo(RoleName.WH_WORKER.name());
        assertThat(response.getEmail()).isNull();
        assertThat(response.getWorkerCode()).isEqualTo("WW-1001");
        then(mailService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("중복 이메일로 초대 시도 시 예외 발생")
    void invite_duplicateEmail_throwsException() {
        given(accountRepository.existsByEmail("manager@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.invite(warehouseManagerRequest, "ACC-INVITER", "TENANT-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("존재하지 않는 창고로 WH_MANAGER 초대 시 예외 발생")
    void invite_invalidWarehouse_throwsException() {
        given(accountRepository.existsByEmail("manager@example.com")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(false);

        assertThatThrownBy(() -> authService.invite(warehouseManagerRequest, "ACC-INVITER", "TENANT-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFERENCE));
    }

    @Test
    @DisplayName("존재하지 않는 셀러로 SELLER 초대 시 예외 발생")
    void invite_invalidSeller_throwsException() {
        given(accountRepository.existsByEmail("seller@example.com")).willReturn(false);
        given(sellerRepository.findById("SELLER-001")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.invite(sellerRequest, "ACC-INVITER", "TENANT-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFERENCE));
    }

    @Test
    @DisplayName("유효하지 않은 역할명으로 초대 시 예외 발생")
    void invite_invalidRoleName_throwsException() {
        warehouseManagerRequest.setRole("INVALID_ROLE");

        assertThatThrownBy(() -> authService.invite(warehouseManagerRequest, "ACC-INVITER", "TENANT-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }
}
