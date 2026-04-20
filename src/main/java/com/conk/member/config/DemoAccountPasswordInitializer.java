package com.conk.member.config;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * 데모 계정(ACC-DEMO-001~005)의 password_hash를 BCrypt("1234")로 보정한다.
 * SQL init 파일의 해시값이 실제 BCrypt("1234")와 다를 수 있어,
 * 서비스 기동 시점에 passwordEncoder로 직접 생성한 해시로 덮어쓴다.
 */
@Component
public class DemoAccountPasswordInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoAccountPasswordInitializer.class);
    private static final String DEMO_MASTER_ACCOUNT_ID = "ACC-DEMO-002";
    private static final String DEMO_MASTER_EMAIL = "master.admin@conk.com";
    private static final String DEMO_TENANT_ID = "TENANT-DEMO-001";
    private static final String SYSTEM_ACTOR = "SYSTEM";

    private static final List<String> DEMO_ACCOUNT_IDS = List.of(
            "ACC-DEMO-001",
            "ACC-DEMO-002",
            "ACC-DEMO-003",
            "ACC-DEMO-004",
            "ACC-DEMO-005"
    );

    private static final String DEMO_PASSWORD = "1234";

    private final AccountRepository accountRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAccountPasswordInitializer(
            AccountRepository accountRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDemoPasswords() {
        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);
        ensureDemoMasterAdmin(encodedPassword);
        int updated = 0;

        for (String accountId : DEMO_ACCOUNT_IDS) {
            Account account = accountRepository.findById(accountId).orElse(null);
            if (account == null) {
                log.debug("[DemoInit] 계정 없음: {}", accountId);
                continue;
            }
            account.setPasswordHash(encodedPassword);
            updated++;
            log.info("[DemoInit] 비밀번호 갱신: accountId={}", accountId);
        }

        log.info("[DemoInit] 데모 계정 비밀번호 초기화 완료: {}개 갱신", updated);
    }

    private void ensureDemoMasterAdmin(String encodedPassword) {
        Account masterAdmin = accountRepository.findByEmail(DEMO_MASTER_EMAIL)
                .or(() -> accountRepository.findById(DEMO_MASTER_ACCOUNT_ID))
                .orElse(null);

        if (masterAdmin != null) {
            masterAdmin.setPasswordHash(encodedPassword);
            log.info("[DemoInit] 총괄관리자 데모 계정 비밀번호 보정: email={}", DEMO_MASTER_EMAIL);
            return;
        }

        Tenant tenant = tenantRepository.findById(DEMO_TENANT_ID).orElse(null);
        Role masterRole = roleRepository.findByRoleName(RoleName.MASTER_ADMIN).orElse(null);

        if (tenant == null || masterRole == null) {
            log.warn("[DemoInit] 총괄관리자 데모 계정 생성 건너뜀: tenant={}, role={}", tenant != null, masterRole != null);
            return;
        }

        Account created = Account.createEmailAccount(
                masterRole,
                tenant,
                null,
                null,
                "총괄 관리자",
                DEMO_MASTER_EMAIL,
                encodedPassword,
                null,
                AccountStatus.ACTIVE,
                false,
                SYSTEM_ACTOR
        );
        created.setAccountId(DEMO_MASTER_ACCOUNT_ID);

        accountRepository.save(created);
        log.info("[DemoInit] 총괄관리자 데모 계정 생성: accountId={}, email={}", DEMO_MASTER_ACCOUNT_ID, DEMO_MASTER_EMAIL);
    }
}
