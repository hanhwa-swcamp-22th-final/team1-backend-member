package com.conk.member.common.security;

/*
 * JWT 필터가 accountId를 넘겨주면 실제 Account를 조회해서
 * Spring Security가 이해할 수 있는 UserDetails로 바꿔주는 서비스다.
 */

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다: " + accountId));

        return new User(
                account.getAccountId(),
                account.getPasswordHash() != null ? account.getPasswordHash() : "",
                Collections.singleton(new SimpleGrantedAuthority(account.getRole().getRoleName().name()))
        );
    }
}
