package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthService {

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;

  public AuthService(
      AccountRepository accountRepository,
      PasswordHasher passwordHasher,
      TokenProvider tokenProvider
  ) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
  }

  @Transactional
  public LoginResponse login(LoginRequest request) {
    String loginId = request.getLoginId();
    String rawPassword = request.getPassword();

    Optional<Account> optionalAccount = isEmail(loginId)
        ? accountRepository.findByEmail(loginId)
        : accountRepository.findByWorkerCode(loginId);

    Account account = optionalAccount.orElseThrow(
        () -> new IllegalArgumentException("존재하지 않는 계정입니다.")
    );

    if (account.getAccountStatus() == AccountStatus.INVITED) {
      throw new IllegalStateException("아직 활성화되지 않은 계정입니다.");
    }

    if (account.getAccountStatus() == AccountStatus.LOCKED) {
      throw new IllegalStateException("잠긴 계정입니다.");
    }

    boolean matched = passwordHasher.matches(
        rawPassword,
        account.getPasswordHash()
    );

    if (!matched) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    account.updateLastLoginAt(LocalDateTime.now(), loginId);

    String accessToken = tokenProvider.createAccessToken(account);

    return new LoginResponse(
        account.getAccountId(),
        account.getAccountName(),
        account.getEmail(),
        account.getWorkerCode(),
        account.getRole().getRoleName(),
        account.getAccountStatus().name(),
        account.getTenant().getTenantName(),
        accessToken
    );
  }

  private boolean isEmail(String loginId) {
    return loginId != null && loginId.contains("@");
  }

}