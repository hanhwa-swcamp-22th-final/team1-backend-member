package com.conk.member.config;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
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

    private static final List<String> DEMO_ACCOUNT_IDS = List.of(
            "ACC-DEMO-001",
            "ACC-DEMO-002",
            "ACC-DEMO-003",
            "ACC-DEMO-004",
            "ACC-DEMO-005"
    );

    private static final String DEMO_PASSWORD = "1234";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAccountPasswordInitializer(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDemoPasswords() {
        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);
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
}
