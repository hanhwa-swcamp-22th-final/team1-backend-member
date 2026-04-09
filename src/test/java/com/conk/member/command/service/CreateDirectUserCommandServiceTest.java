package com.conk.member.command.service;

import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.service.UserService;
import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.infrastructure.service.PasswordService;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDirectUserCommandServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordService passwordService;
    @Mock WarehouseService warehouseService;

    @InjectMocks UserService userService;

    private Role warehouseWorkerRole;
    private CreateDirectUserRequest request;

    @BeforeEach
    void setUp() {
        warehouseWorkerRole = new Role();
        warehouseWorkerRole.setRoleId("ROLE-003");
        warehouseWorkerRole.setRoleName(RoleName.WAREHOUSE_WORKER);

        request = new CreateDirectUserRequest();
        request.setTenantId("TENANT-001");
        request.setWarehouseId("WH-001");
        request.setName("홍길동");
        request.setWorkerCode("WC-001");
        request.setPassword("password123");
        request.setEmail("worker@example.com");
        request.setPhoneNo("010-1234-5678");
    }

    @Test
    @DisplayName("직접 사용자 생성 성공")
    void createDirect_success() {
        given(accountRepository.existsByWorkerCode("WC-001")).willReturn(false);
        given(accountRepository.existsByEmail("worker@example.com")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(true);
        given(roleRepository.findByRoleName(RoleName.WAREHOUSE_WORKER)).willReturn(Optional.of(warehouseWorkerRole));
        given(passwordService.encode("password123")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        CreateDirectUserResponse response = userService.createDirect(request);

        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getWorkerCode()).isEqualTo("WC-001");
        assertThat(response.getTenantId()).isEqualTo("TENANT-001");
        assertThat(response.getWarehouseId()).isEqualTo("WH-001");
        assertThat(response.getRole()).isEqualTo(RoleName.WAREHOUSE_WORKER.name());
        assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE.name());
    }

    @Test
    @DisplayName("이메일 없이 생성 성공 (이메일 중복 검사 스킵)")
    void createDirect_withoutEmail_success() {
        request.setEmail(null);

        given(accountRepository.existsByWorkerCode("WC-001")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(true);
        given(roleRepository.findByRoleName(RoleName.WAREHOUSE_WORKER)).willReturn(Optional.of(warehouseWorkerRole));
        given(passwordService.encode("password123")).willReturn("$2a$encoded");
        given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

        CreateDirectUserResponse response = userService.createDirect(request);

        assertThat(response.getName()).isEqualTo("홍길동");
        then(accountRepository).should(never()).existsByEmail(any());
    }

    @Test
    @DisplayName("중복 작업자 코드로 생성 실패")
    void createDirect_duplicateWorkerCode_throwsException() {
        given(accountRepository.existsByWorkerCode("WC-001")).willReturn(true);

        assertThatThrownBy(() -> userService.createDirect(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_WORKER_CODE));
    }

    @Test
    @DisplayName("중복 이메일로 생성 실패")
    void createDirect_duplicateEmail_throwsException() {
        given(accountRepository.existsByWorkerCode("WC-001")).willReturn(false);
        given(accountRepository.existsByEmail("worker@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.createDirect(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("존재하지 않는 창고로 생성 실패")
    void createDirect_invalidWarehouse_throwsException() {
        given(accountRepository.existsByWorkerCode("WC-001")).willReturn(false);
        given(accountRepository.existsByEmail("worker@example.com")).willReturn(false);
        given(warehouseService.exists("WH-001")).willReturn(false);

        assertThatThrownBy(() -> userService.createDirect(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFERENCE));
    }
}
