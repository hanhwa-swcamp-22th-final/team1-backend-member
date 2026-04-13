package com.conk.member.common.config;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(DevDataInitializerTest.TestConfig.class)
class DevDataInitializerTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private DevDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DevDataInitializer(
                roleRepository,
                tenantRepository,
                sellerRepository,
                accountRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("빈 개발용 데이터베이스가 주어지면 시드 초기화를 했을 때 기본 역할과 계정이 생성되어야 한다")
    void initialize_whenDatabaseIsEmpty_createsDefaultRolesAndAccounts() {
        initializer.initialize();

        assertThat(roleRepository.count()).isEqualTo(5);
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(sellerRepository.count()).isEqualTo(1);
        assertThat(accountRepository.count()).isEqualTo(5);

        assertEmailAccountExists("sys.admin@conk.com");
        assertEmailAccountExists("master.admin@conk.com");
        assertEmailAccountExists("wh.manager@conk.com");
        assertEmailAccountExists("seller@conk.com");
        assertThat(accountRepository.findByWorkerCode("WORKER-001")).isPresent();
    }

    @Test
    @DisplayName("기본 시드 데이터가 이미 존재하면 시드 초기화를 다시 했을 때 중복 없이 기존 구성이 유지되어야 한다")
    void initialize_whenSeedAlreadyExists_keepsDataWithoutDuplication() {
        initializer.initialize();
        initializer.initialize();

        assertThat(roleRepository.count()).isEqualTo(5);
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(sellerRepository.count()).isEqualTo(1);
        assertThat(accountRepository.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("기본 계정이 생성되면 비밀번호를 검증했을 때 모든 계정이 평문 1234로 로그인 가능해야 한다")
    void initialize_whenAccountsAreCreated_storesPasswordsAsValidHashes() {
        initializer.initialize();

        assertPasswordMatches("sys.admin@conk.com");
        assertPasswordMatches("master.admin@conk.com");
        assertPasswordMatches("wh.manager@conk.com");
        assertPasswordMatches("seller@conk.com");

        Account worker = accountRepository.findByWorkerCode("WORKER-001").orElseThrow();
        assertThat(worker.getPasswordHash()).isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", worker.getPasswordHash())).isTrue();
    }

    private void assertEmailAccountExists(String email) {
        assertThat(accountRepository.findByEmail(email)).isPresent();
    }

    private void assertPasswordMatches(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo("1234");
        assertThat(passwordEncoder.matches("1234", account.getPasswordHash())).isTrue();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
