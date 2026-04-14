package com.conk.member.command.repository;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired AccountRepository accountRepository;
    @Autowired RoleRepository roleRepository;

    private Role masterAdminRole;
    private Role workerRole;

    @BeforeEach
    void setUp() {
        masterAdminRole = new Role();
        masterAdminRole.setRoleId("ROLE-001");
        masterAdminRole.setRoleName(RoleName.MASTER_ADMIN);
        masterAdminRole.setRoleDescription("총괄관리자");
        masterAdminRole.setCreatedBy("system");
        masterAdminRole.setUpdatedBy("system");
        roleRepository.save(masterAdminRole);

        workerRole = new Role();
        workerRole.setRoleId("ROLE-003");
        workerRole.setRoleName(RoleName.WH_WORKER);
        workerRole.setRoleDescription("창고작업자");
        workerRole.setCreatedBy("system");
        workerRole.setUpdatedBy("system");
        roleRepository.save(workerRole);
    }

    private Account buildEmailAccount(String id, String email, AccountStatus status) {
        Account account = new Account();
        account.setAccountId(id);
        account.setRole(masterAdminRole);
        account.setTenantId("TENANT-001");
        account.setAccountName("테스트유저");
        account.setEmail(email);
        account.setPasswordHash("$2a$encoded");
        account.setAccountStatus(status);
        account.setIsTemporaryPassword(false);
        account.setCreatedBy("system");
        account.setUpdatedBy("system");
        return account;
    }

    private Account buildWorkerAccount(String id, String workerCode) {
        Account account = new Account();
        account.setAccountId(id);
        account.setRole(workerRole);
        account.setTenantId("TENANT-001");
        account.setWarehouseId("WH-001");
        account.setAccountName("작업자");
        account.setWorkerCode(workerCode);
        account.setPasswordHash("$2a$encoded");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(false);
        account.setCreatedBy("system");
        account.setUpdatedBy("system");
        return account;
    }

    @Test
    @DisplayName("이메일로 계정 조회")
    void findByEmail_success() {
        accountRepository.save(buildEmailAccount("ACC-001", "test@example.com", AccountStatus.ACTIVE));

        Optional<Account> result = accountRepository.findByEmail("test@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회 시 빈 Optional 반환")
    void findByEmail_notFound_returnsEmpty() {
        Optional<Account> result = accountRepository.findByEmail("none@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("작업자 코드로 계정 조회")
    void findByWorkerCode_success() {
        accountRepository.save(buildWorkerAccount("ACC-002", "WC-001"));

        Optional<Account> result = accountRepository.findByWorkerCode("WC-001");

        assertThat(result).isPresent();
        assertThat(result.get().getWorkerCode()).isEqualTo("WC-001");
    }

    @Test
    @DisplayName("이메일 중복 여부 확인 - 존재하는 경우")
    void existsByEmail_exists_returnsTrue() {
        accountRepository.save(buildEmailAccount("ACC-001", "test@example.com", AccountStatus.ACTIVE));

        assertThat(accountRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_notExists_returnsFalse() {
        assertThat(accountRepository.existsByEmail("none@example.com")).isFalse();
    }

    @Test
    @DisplayName("작업자 코드 중복 여부 확인")
    void existsByWorkerCode_exists_returnsTrue() {
        accountRepository.save(buildWorkerAccount("ACC-002", "WC-001"));

        assertThat(accountRepository.existsByWorkerCode("WC-001")).isTrue();
    }

    @Test
    @DisplayName("테넌트/역할/상태별 계정 수 조회")
    void countByTenantIdAndRoleNameAndAccountStatus() {
        accountRepository.save(buildEmailAccount("ACC-001", "admin1@example.com", AccountStatus.ACTIVE));
        accountRepository.save(buildEmailAccount("ACC-002", "admin2@example.com", AccountStatus.ACTIVE));
        accountRepository.save(buildEmailAccount("ACC-003", "admin3@example.com", AccountStatus.INACTIVE));

        long activeCount = accountRepository.countByTenantIdAndRoleNameAndAccountStatus(
                "TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE);

        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("검색 쿼리 - 키워드로 계정 목록 조회")
    void search_byKeyword_returnsMatchingAccounts() {
        Account acc1 = buildEmailAccount("ACC-001", "alice@example.com", AccountStatus.ACTIVE);
        acc1.setAccountName("Alice");
        Account acc2 = buildEmailAccount("ACC-002", "bob@example.com", AccountStatus.ACTIVE);
        acc2.setAccountName("Bob");
        accountRepository.save(acc1);
        accountRepository.save(acc2);

        List<Account> result = accountRepository.search("TENANT-001", null, null, null, null, "Alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("검색 쿼리 - 전체 조건 null이면 전체 반환")
    void search_noCondition_returnsAll() {
        accountRepository.save(buildEmailAccount("ACC-001", "a@example.com", AccountStatus.ACTIVE));
        accountRepository.save(buildEmailAccount("ACC-002", "b@example.com", AccountStatus.ACTIVE));

        List<Account> result = accountRepository.search("TENANT-001", null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("테넌트별 창고 ID 목록 조회")
    void findDistinctWarehouseIdsByTenantId() {
        Account worker1 = buildWorkerAccount("ACC-003", "WC-001");
        worker1.setWarehouseId("WH-001");
        Account worker2 = buildWorkerAccount("ACC-004", "WC-002");
        worker2.setWarehouseId("WH-002");
        Account worker3 = buildWorkerAccount("ACC-005", "WC-003");
        worker3.setWarehouseId("WH-001"); // 중복
        accountRepository.save(worker1);
        accountRepository.save(worker2);
        accountRepository.save(worker3);

        List<String> warehouseIds = accountRepository.findDistinctWarehouseIdsByTenantId("TENANT-001");

        assertThat(warehouseIds).hasSize(2);
        assertThat(warehouseIds).containsExactlyInAnyOrder("WH-001", "WH-002");
    }
}
