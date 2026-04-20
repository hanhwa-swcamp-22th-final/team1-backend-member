package com.conk.member.config;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DemoAccountPasswordInitializerTest {

    @Mock AccountRepository accountRepository;
    @Mock TenantRepository tenantRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks DemoAccountPasswordInitializer initializer;

    @Test
    @DisplayName("총괄관리자 데모 계정이 없으면 생성하고 1234 비밀번호를 설정한다")
    void initDemoPasswords_createsMissingMasterAdmin() {
        Tenant tenant = Tenant.create(
                "TENANT-DEMO-001",
                "TEN-CONK-DEMO-001",
                "CONK Demo Logistics",
                "데모 대표",
                null,
                null,
                null,
                null,
                "3PL",
                "SYSTEM"
        );

        Role role = new Role();
        role.setRoleId("ROLE-002");
        role.setRoleName(RoleName.MASTER_ADMIN);

        given(passwordEncoder.encode("1234")).willReturn("encoded-1234");
        given(accountRepository.findByEmail("master.admin@conk.com")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-002")).willReturn(Optional.empty());
        given(tenantRepository.findById("TENANT-DEMO-001")).willReturn(Optional.of(tenant));
        given(roleRepository.findByRoleName(RoleName.MASTER_ADMIN)).willReturn(Optional.of(role));
        given(accountRepository.findById("ACC-DEMO-001")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-003")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-004")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-005")).willReturn(Optional.empty());

        initializer.initDemoPasswords();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        then(accountRepository).should().save(captor.capture());

        Account saved = captor.getValue();
        assertThat(saved.getAccountId()).isEqualTo("ACC-DEMO-002");
        assertThat(saved.getEmail()).isEqualTo("master.admin@conk.com");
        assertThat(saved.getAccountName()).isEqualTo("총괄 관리자");
        assertThat(saved.getTenantId()).isEqualTo("TENANT-DEMO-001");
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-1234");
        assertThat(saved.getRole().getRoleName()).isEqualTo(RoleName.MASTER_ADMIN);
    }

    @Test
    @DisplayName("총괄관리자 데모 계정이 이미 있으면 비밀번호만 갱신한다")
    void initDemoPasswords_updatesExistingMasterAdminPassword() {
        Account existing = new Account();
        existing.setAccountId("ACC-DEMO-002");
        existing.setEmail("master.admin@conk.com");
        existing.setPasswordHash("old");

        given(passwordEncoder.encode("1234")).willReturn("encoded-1234");
        given(accountRepository.findByEmail("master.admin@conk.com")).willReturn(Optional.of(existing));
        given(accountRepository.findById("ACC-DEMO-001")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-002")).willReturn(Optional.of(existing));
        given(accountRepository.findById("ACC-DEMO-003")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-004")).willReturn(Optional.empty());
        given(accountRepository.findById("ACC-DEMO-005")).willReturn(Optional.empty());

        initializer.initDemoPasswords();

        assertThat(existing.getPasswordHash()).isEqualTo("encoded-1234");
        then(accountRepository).shouldHaveNoMoreInteractions();
    }
}
