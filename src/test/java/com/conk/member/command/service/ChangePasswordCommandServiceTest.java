package com.conk.member.command.service;

import com.conk.member.command.application.dto.request.ChangePasswordRequest;
import com.conk.member.command.application.dto.response.ChangePasswordResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.InvitationRepository;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import com.conk.member.command.domain.repository.RefreshTokenRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.command.infrastructure.mail.MailService;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.TokenService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ChangePasswordCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock SellerRepository sellerRepository;
    @Mock InvitationRepository invitationRepository;
    @Mock RoleRepository roleRepository;
    @Mock TenantRepository tenantRepository;
    @Mock MemberTokenRepository memberTokenRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordService passwordService;
    @Mock TokenService tokenService;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock MailService mailService;
    @Mock WarehouseService warehouseService;

    @InjectMocks AuthService authService;

    private Account account;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId("ROLE-001");
        role.setRoleName(RoleName.MASTER_ADMIN);

        account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(role);
        account.setAccountStatus(AccountStatus.TEMP_PASSWORD);
        account.setPasswordHash("$2a$temp");
    }

    @Test
    @DisplayName("임시 비밀번호 상태 계정은 비밀번호 변경 성공 후 ACTIVE가 된다")
    void changePassword_tempPasswordAccount_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPassword123!");

        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));
        given(passwordService.encode("newPassword123!")).willReturn("$2a$new");

        ChangePasswordResponse response = authService.changePassword("ACC-001", request);

        assertThat(response.getAccountId()).isEqualTo("ACC-001");
        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE.name());
        assertThat(response.getPasswordChangedAt()).isNotNull();
        then(accountRepository).should().save(account);
    }

    @Test
    @DisplayName("임시 비밀번호 상태가 아닌 계정은 비밀번호 변경이 거부된다")
    void changePassword_nonTempPasswordAccount_forbidden() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPassword123!");
        account.setAccountStatus(AccountStatus.ACTIVE);

        given(accountRepository.findById("ACC-001")).willReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.changePassword("ACC-001", request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("로그인 사용자 정보가 없으면 비밀번호 변경이 거부된다")
    void changePassword_missingAccountId_unauthorized() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPassword123!");

        assertThatThrownBy(() -> authService.changePassword(null, request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}
