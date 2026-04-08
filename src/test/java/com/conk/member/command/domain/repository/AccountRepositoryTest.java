package com.conk.member.command.domain.repository;

/*
 * AccountRepository의 핵심 조회/검색 쿼리를 검증하는 JPA 레포지토리 테스트다.
 * 로그인 식별자 조회, 중복 확인, 마지막 활성 총괄관리자 계산이 정상 동작하는지 확인한다.
 */

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("이메일과 작업자 코드로 계정을 찾을 수 있다")
    void find_by_email_and_worker_code() {
        Role workerRole = saveRole("ROLE-WORKER", RoleName.WAREHOUSE_WORKER);
        Account account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(workerRole);
        account.setTenantId("TENANT-001");
        account.setAccountName("현장 작업자");
        account.setEmail("worker@conk.com");
        account.setWorkerCode("WORKER-001");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);

        assertThat(accountRepository.findByEmail("worker@conk.com")).isPresent();
        assertThat(accountRepository.findByWorkerCode("WORKER-001")).isPresent();
        assertThat(accountRepository.existsByEmail("worker@conk.com")).isTrue();
        assertThat(accountRepository.existsByWorkerCode("WORKER-001")).isTrue();
    }

    @Test
    @DisplayName("업체별 활성 총괄관리자 수를 계산할 수 있다")
    void count_active_master_admin() {
        Role masterRole = saveRole("ROLE-MASTER", RoleName.MASTER_ADMIN);
        accountRepository.save(createAccount("ACC-101", masterRole, "TENANT-001", AccountStatus.ACTIVE, "master1@conk.com", null));
        accountRepository.save(createAccount("ACC-102", masterRole, "TENANT-001", AccountStatus.ACTIVE, "master2@conk.com", null));
        accountRepository.save(createAccount("ACC-103", masterRole, "TENANT-001", AccountStatus.INACTIVE, "master3@conk.com", null));

        long count = accountRepository.countByTenantIdAndRoleNameAndAccountStatus("TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("조건으로 사용자 목록을 검색할 수 있다")
    void search_users() {
        Role sellerRole = saveRole("ROLE-SELLER", RoleName.SELLER);
        Role managerRole = saveRole("ROLE-MANAGER", RoleName.WAREHOUSE_MANAGER);
        accountRepository.save(createAccount("ACC-201", sellerRole, "TENANT-001", AccountStatus.ACTIVE, "seller@conk.com", null));
        accountRepository.save(createAccount("ACC-202", managerRole, "TENANT-001", AccountStatus.ACTIVE, "manager@conk.com", "WH-001"));

        List<Account> accounts = accountRepository.search("TENANT-001", "WAREHOUSE_MANAGER", "ACTIVE", null, "WH-001", "manager");

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getEmail()).isEqualTo("manager@conk.com");
    }

    private Role saveRole(String roleId, RoleName roleName) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName(roleName);
        role.setRoleDescription(roleName.name());
        role.setIsActive(1);
        return roleRepository.save(role);
    }

    private Account createAccount(String accountId, Role role, String tenantId, AccountStatus status, String email, String warehouseId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole(role);
        account.setTenantId(tenantId);
        account.setWarehouseId(warehouseId);
        account.setAccountName(email.split("@")[0]);
        account.setEmail(email);
        account.setAccountStatus(status);
        account.setIsTemporaryPassword(Boolean.FALSE);
        return account;
    }
}
