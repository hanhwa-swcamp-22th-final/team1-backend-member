package com.conk.member.command.infrastructure.repository;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("계정을 저장할 수 있다")
    void save_account() {
        Role role = saveRole("ROLE-001", "MASTER_ADMIN");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "홍길동",
                "master@conk.com",
                "hashed-password",
                "010-1111-2222",
                AccountStatus.ACTIVE,
                false,
                "system"
        );

        Account saved = accountRepository.save(account);

        assertThat(saved.getAccountId()).isNotNull();

        Optional<Account> found = accountRepository.findById(saved.getAccountId());
        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("홍길동");
        assertThat(found.get().getEmail()).isEqualTo("master@conk.com");
        assertThat(found.get().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("이메일로 계정을 조회할 수 있다")
    void find_by_email() {
        Role role = saveRole("ROLE-001", "MASTER_ADMIN");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "관리자",
                "admin@conk.com",
                "hashed-password",
                "010-1234-5678",
                AccountStatus.ACTIVE,
                false,
                "system"
        );

        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        Optional<Account> found = accountRepository.findByEmail("admin@conk.com");

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("관리자");
        assertThat(found.get().getEmail()).isEqualTo("admin@conk.com");
    }

    @Test
    @DisplayName("workerCode로 작업자 계정을 조회할 수 있다")
    void find_by_worker_code() {
        Role role = saveRole("ROLE-002", "WAREHOUSE_WORKER");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account worker = Account.createWorkerAccount(
                role,
                tenant,
                "WH-001",
                "작업자A",
                "WK-001",
                "hashed-password",
                "010-9999-0000",
                "system"
        );

        entityManager.persist(worker);
        entityManager.flush();
        entityManager.clear();

        Optional<Account> found = accountRepository.findByWorkerCode("WK-001");

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("작업자A");
        assertThat(found.get().getWorkerCode()).isEqualTo("WK-001");
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void exists_by_email() {
        Role role = saveRole("ROLE-001", "MASTER_ADMIN");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "관리자",
                "exists@conk.com",
                "hashed-password",
                "010-0000-0000",
                AccountStatus.ACTIVE,
                false,
                "system"
        );

        entityManager.persist(account);
        entityManager.flush();
        entityManager.clear();

        boolean result = accountRepository.existsByEmail("exists@conk.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("workerCode 존재 여부를 확인할 수 있다")
    void exists_by_worker_code() {
        Role role = saveRole("ROLE-002", "WAREHOUSE_WORKER");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account worker = Account.createWorkerAccount(
                role,
                tenant,
                "WH-001",
                "작업자B",
                "WK-777",
                "hashed-password",
                "010-3333-4444",
                "system"
        );

        entityManager.persist(worker);
        entityManager.flush();
        entityManager.clear();

        boolean result = accountRepository.existsByWorkerCode("WK-777");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("계정 정보를 수정할 수 있다")
    void update_account() {
        Role role = saveRole("ROLE-001", "MASTER_ADMIN");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "관리자",
                "update@conk.com",
                "hashed-password",
                "010-0000-1111",
                AccountStatus.TEMP_PASSWORD,
                true,
                "system"
        );

        Account saved = accountRepository.save(account);

        saved.setUpdatedBy("admin");
        saved.successLogin();
        saved.changePassword("new-hashed-password");
        accountRepository.save(saved);

        entityManager.flush();
        entityManager.clear();

        Account found = accountRepository.findById(saved.getAccountId()).orElseThrow();
        assertThat(found.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(found.getIsTemporaryPassword()).isFalse();
        assertThat(found.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(found.getLastLoginAt()).isNotNull();
        assertThat(found.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("계정을 삭제할 수 있다")
    void delete_account() {
        Role role = saveRole("ROLE-001", "MASTER_ADMIN");
        Tenant tenant = saveTenant("TENANT-001", "TENANT_CODE_001", "콘크3PL");

        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "삭제대상",
                "delete@conk.com",
                "hashed-password",
                "010-1212-3434",
                AccountStatus.ACTIVE,
                false,
                "system"
        );

        Account saved = accountRepository.save(account);

        accountRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<Account> found = accountRepository.findById(saved.getAccountId());
        assertThat(found).isEmpty();
    }

    private Role saveRole(String roleId, String roleName) {
        Role role = Role.create(
                roleId,
                roleName,
                roleName + " 설명",
                "system"
        );
        entityManager.persist(role);
        return role;
    }

    private Tenant saveTenant(String tenantId, String tenantCode, String tenantName) {
        Tenant tenant = Tenant.create(
                tenantId,
                tenantCode,
                tenantName,
                "대표자",
                "123-45-67890",
                "02-1111-2222",
                "tenant@conk.com",
                "서울",
                "GENERAL",
                "system"
        );
        entityManager.persist(tenant);
        return tenant;
    }
}
