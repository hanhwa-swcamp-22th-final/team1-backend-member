package com.conk.member.command.application.service;

/*
 * 로그인 로직만 분리해서 검증하는 단위 테스트다.
 * 이메일 로그인, 작업자 코드 로그인, 비밀번호 오류 케이스를 각각 확인한다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.*;
import com.conk.member.command.infrastructure.service.*;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandLoginServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private InvitationRepository invitationRepository;
    @Mock private MemberTokenRepository memberTokenRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RolePermissionRepository rolePermissionRepository;
    @Mock private RolePermissionHistoryRepository rolePermissionHistoryRepository;
    @Mock private PasswordSupport passwordSupport;
    @Mock private TokenSupport tokenSupport;
    @Mock private MailSupport mailSupport;
    @Mock private WarehouseSupport warehouseSupport;

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("이메일로 로그인하면 토큰과 사용자 정보를 반환한다")
    void login_with_email_success() {
        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("master@conk.com");
        request.setPassword("raw-password");

        Account account = createAccount("ACC-001", "master@conk.com", null, RoleName.MASTER_ADMIN);
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantName("FASTSHIP LOGISTICS");

        when(accountRepository.findByEmail("master@conk.com")).thenReturn(Optional.of(account));
        when(passwordSupport.matches("raw-password", "encoded-password")).thenReturn(true);
        when(tokenSupport.createAccessToken(eq("ACC-001"), eq("MASTER_ADMIN"))).thenReturn("access-token");
        when(tenantRepository.findById("TENANT-001")).thenReturn(Optional.of(tenant));

        MemberResponses.LoginResponse response = memberCommandService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo("master@conk.com");
        assertThat(response.getTenantName()).isEqualTo("FASTSHIP LOGISTICS");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("이메일 계정이 없으면 작업자 코드로도 로그인할 수 있다")
    void login_with_worker_code_success() {
        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("WORKER-001");
        request.setPassword("raw-password");

        Account account = createAccount("ACC-002", null, "WORKER-001", RoleName.WAREHOUSE_WORKER);
        account.setWarehouseId("WH-001");

        when(accountRepository.findByEmail("WORKER-001")).thenReturn(Optional.empty());
        when(accountRepository.findByWorkerCode("WORKER-001")).thenReturn(Optional.of(account));
        when(passwordSupport.matches("raw-password", "encoded-password")).thenReturn(true);
        when(tokenSupport.createAccessToken(eq("ACC-002"), eq("WAREHOUSE_WORKER"))).thenReturn("worker-token");

        MemberResponses.LoginResponse response = memberCommandService.login(request);

        assertThat(response.getToken()).isEqualTo("worker-token");
        assertThat(response.getWarehouseId()).isEqualTo("WH-001");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void login_fail_when_password_not_match() {
        MemberRequests.LoginRequest request = new MemberRequests.LoginRequest();
        request.setEmailOrWorkerCode("master@conk.com");
        request.setPassword("wrong-password");

        Account account = createAccount("ACC-001", "master@conk.com", null, RoleName.MASTER_ADMIN);
        when(accountRepository.findByEmail("master@conk.com")).thenReturn(Optional.of(account));
        when(passwordSupport.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> memberCommandService.login(request))
            .isInstanceOf(MemberException.class);
    }

    private Account createAccount(String accountId, String email, String workerCode, RoleName roleName) {
        Role role = new Role();
        role.setRoleId("ROLE-001");
        role.setRoleName(roleName);

        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole(role);
        account.setTenantId("TENANT-001");
        account.setAccountName("테스트 사용자");
        account.setEmail(email);
        account.setWorkerCode(workerCode);
        account.setPasswordHash("encoded-password");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        return account;
    }
}
