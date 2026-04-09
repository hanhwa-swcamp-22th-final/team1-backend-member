package com.conk.member.command.service;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.UserService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
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
class DeactivateUserCommandServiceTest {

    @Mock AccountRepository accountRepository;

    @InjectMocks UserService userService;

    private Account account;
    private Role masterAdminRole;
    private Role workerRole;

    @BeforeEach
    void setUp() {
        masterAdminRole = new Role();
        masterAdminRole.setRoleId("ROLE-001");
        masterAdminRole.setRoleName(RoleName.MASTER_ADMIN);

        workerRole = new Role();
        workerRole.setRoleId("ROLE-003");
        workerRole.setRoleName(RoleName.WAREHOUSE_WORKER);

        account = new Account();
        account.setAccountId("ACC-001");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setTenantId("TENANT-001");
        account.setIsTemporaryPassword(false);
    }

    @Test
    @DisplayName("일반 사용자 비활성화 성공")
    void deactivate_regularUser_success() {
        account.setRole(workerRole);
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        SimpleUserStatusResponse response = userService.deactivate("ACC-001");

        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE.name());
        then(accountRepository).should().save(account);
    }

    @Test
    @DisplayName("마지막 활성 MASTER_ADMIN 비활성화 시도 시 예외 발생")
    void deactivate_lastActiveMasterAdmin_throwsException() {
        account.setRole(masterAdminRole);
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                "TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE)).willReturn(1L);

        assertThatThrownBy(() -> userService.deactivate("ACC-001"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode())
                        .isEqualTo(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED));
    }

    @Test
    @DisplayName("MASTER_ADMIN이 여럿이면 비활성화 성공")
    void deactivate_masterAdminWithMultiple_success() {
        account.setRole(masterAdminRole);
        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                "TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE)).willReturn(2L);
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        SimpleUserStatusResponse response = userService.deactivate("ACC-001");

        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE.name());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 비활성화 시도 시 예외 발생")
    void deactivate_userNotFound_throwsException() {
        given(accountRepository.findById("ACC-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivate("ACC-999"))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }
}
