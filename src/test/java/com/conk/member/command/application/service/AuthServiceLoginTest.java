package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordHasher passwordHasher;

  @Mock
  private TokenProvider tokenProvider;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(accountRepository, passwordHasher, tokenProvider);
  }

  @Test
  @DisplayName("이메일로 로그인할 수 있다")
  void login_with_email_success() {
    LoginRequest request = new LoginRequest("admin@conk.com", "raw-password");
    Account account = spy(activeAccount());

    when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));
    when(passwordHasher.matches("raw-password", "encoded-password")).thenReturn(true);
    when(tokenProvider.createAccessToken(account)).thenReturn("access-token");

    LoginResponse response = authService.login(request);

    assertThat(response).isNotNull();
    assertThat(response.getAccountId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("관리자");
    assertThat(response.getEmail()).isEqualTo("admin@conk.com");
    assertThat(response.getWorkerCode()).isEqualTo("WORKER-001");
    assertThat(response.getStatus()).isEqualTo("ACTIVE");
    assertThat(response.getAccessToken()).isEqualTo("access-token");

    verify(accountRepository).findByEmail("admin@conk.com");
    verify(accountRepository, never()).findByWorkerCode(any());
    verify(passwordHasher).matches("raw-password", "encoded-password");
    verify(account).updateLastLoginAt(any(LocalDateTime.class), anyString());
    verify(tokenProvider).createAccessToken(account);
  }

  @Test
  @DisplayName("작업자 코드로 로그인할 수 있다")
  void login_with_worker_code_success() {
    LoginRequest request = new LoginRequest("WORKER-001", "raw-password");
    Account account = spy(activeAccount());

    when(accountRepository.findByWorkerCode("WORKER-001")).thenReturn(Optional.of(account));
    when(passwordHasher.matches("raw-password", "encoded-password")).thenReturn(true);
    when(tokenProvider.createAccessToken(account)).thenReturn("access-token");

    LoginResponse response = authService.login(request);

    assertThat(response).isNotNull();
    assertThat(response.getAccountId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("관리자");
    assertThat(response.getEmail()).isEqualTo("admin@conk.com");
    assertThat(response.getWorkerCode()).isEqualTo("WORKER-001");
    assertThat(response.getStatus()).isEqualTo("ACTIVE");
    assertThat(response.getRole()).isEqualTo("MASTER_ADMIN");
    assertThat(response.getAccessToken()).isEqualTo("access-token");

    verify(accountRepository).findByWorkerCode("WORKER-001");
    verify(accountRepository, never()).findByEmail(any());
    verify(passwordHasher).matches("raw-password", "encoded-password");
    verify(account).updateLastLoginAt(any(LocalDateTime.class), anyString());
    verify(tokenProvider).createAccessToken(account);
  }

  @Test
  @DisplayName("존재하지 않는 계정이면 로그인에 실패한다")
  void login_fail_when_account_not_found() {
    LoginRequest request = new LoginRequest("nobody@conk.com", "raw-password");

    when(accountRepository.findByEmail("nobody@conk.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 계정입니다.");

    verify(accountRepository).findByEmail("nobody@conk.com");
    verify(passwordHasher, never()).matches(any(), any());
    verify(tokenProvider, never()).createAccessToken(any());
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
  void login_fail_when_password_mismatch() {
    LoginRequest request = new LoginRequest("admin@conk.com", "wrong-password");
    Account account = spy(activeAccount());

    when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));
    when(passwordHasher.matches("wrong-password", "encoded-password")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("비밀번호가 일치하지 않습니다.");

    verify(accountRepository).findByEmail("admin@conk.com");
    verify(passwordHasher).matches("wrong-password", "encoded-password");
    verify(account, never()).updateLastLoginAt(any(LocalDateTime.class), anyString());
    verify(tokenProvider, never()).createAccessToken(any());
  }

  @Test
  @DisplayName("INVITED 상태 계정은 로그인할 수 없다")
  void login_fail_when_status_is_invited() {
    LoginRequest request = new LoginRequest("admin@conk.com", "raw-password");
    Account account = spy(invitedAccount());

    when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("아직 활성화되지 않은 계정입니다.");

    verify(accountRepository).findByEmail("admin@conk.com");
    verify(passwordHasher, never()).matches(any(), any());
    verify(account, never()).updateLastLoginAt(any(LocalDateTime.class), anyString());
    verify(tokenProvider, never()).createAccessToken(any());
  }

  @Test
  @DisplayName("LOCKED 상태 계정은 로그인할 수 없다")
  void login_fail_when_status_is_locked() {
    LoginRequest request = new LoginRequest("admin@conk.com", "raw-password");
    Account account = spy(lockedAccount());

    when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("잠긴 계정입니다.");

    verify(accountRepository).findByEmail("admin@conk.com");
    verify(passwordHasher, never()).matches(any(), any());
    verify(account, never()).updateLastLoginAt(any(LocalDateTime.class), anyString());
    verify(tokenProvider, never()).createAccessToken(any());
  }

  @Test
  @DisplayName("로그인 성공 시 lastLoginAt이 갱신된다")
  void update_last_login_at_on_success() {
    LoginRequest request = new LoginRequest("admin@conk.com", "raw-password");
    Account account = spy(activeAccount());

    when(accountRepository.findByEmail("admin@conk.com")).thenReturn(Optional.of(account));
    when(passwordHasher.matches("raw-password", "encoded-password")).thenReturn(true);
    when(tokenProvider.createAccessToken(account)).thenReturn("access-token");

    authService.login(request);

    verify(account, times(1)).updateLastLoginAt(any(LocalDateTime.class), anyString());
  }

  private Account activeAccount() {
    return buildAccount(AccountStatus.ACTIVE);
  }

  private Account invitedAccount() {
    return buildAccount(AccountStatus.INVITED);
  }

  private Account lockedAccount() {
    return buildAccount(AccountStatus.LOCKED);
  }

  private Account buildAccount(AccountStatus status) {
    Role role = Role.create("ROLE-001", "MASTER_ADMIN", "총괄 관리자", "system");
    Tenant tenant = Tenant.create(
        "TENANT-001",
        "TENANT_CODE_001",
        "콘크3PL",
        "대표자",
        "123-45-67890",
        "02-1111-2222",
        "tenant@conk.com",
        "서울",
        "GENERAL",
        "system"
    );

    Account account = Account.createEmailAccount(
        role,
        tenant,
        null,
        null,
        "관리자",
        "admin@conk.com",
        "encoded-password",
        "010-1234-5678",
        status,
        false,
        "system"
    );

    try {
      java.lang.reflect.Field field = Account.class.getDeclaredField("accountId");
      field.setAccessible(true);
      field.set(account, 1L);

      java.lang.reflect.Field workerCodeField = Account.class.getDeclaredField("workerCode");
      workerCodeField.setAccessible(true);
      workerCodeField.set(account, "WORKER-001");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return account;
  }
}

