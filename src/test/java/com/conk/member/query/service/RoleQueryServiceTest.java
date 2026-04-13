package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.PermissionHistoryRequest;
import com.conk.member.query.dto.request.RolePermissionMatrixRequest;
import com.conk.member.query.dto.response.PermissionHistoryResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixRowResponse;
import com.conk.member.query.mapper.RolePermissionQueryMapper;
import com.conk.member.query.service.RoleQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RoleQueryServiceTest {

    @Mock RoleRepository roleRepository;
    @Mock RolePermissionQueryMapper rolePermissionQueryMapper;

    @InjectMocks RoleQueryService roleQueryService;

    private Role warehouseManagerRole;
    private Role warehouseWorkerRole;
    private Role masterAdminRole;

    @BeforeEach
    void setUp() {
        warehouseManagerRole = new Role();
        warehouseManagerRole.setRoleId("ROLE-002");
        warehouseManagerRole.setRoleName(RoleName.WAREHOUSE_MANAGER);

        warehouseWorkerRole = new Role();
        warehouseWorkerRole.setRoleId("ROLE-003");
        warehouseWorkerRole.setRoleName(RoleName.WAREHOUSE_WORKER);

        masterAdminRole = new Role();
        masterAdminRole.setRoleId("ROLE-001");
        masterAdminRole.setRoleName(RoleName.MASTER_ADMIN);
    }

    // ─── getRolePermissions ───────────────────────────────────────────────────

    @Test
    @DisplayName("roleId로 권한 매트릭스 조회 성공")
    void getRolePermissions_byRoleId_success() {
        RolePermissionMatrixRowResponse row = new RolePermissionMatrixRowResponse();
        row.setPermissionId("PERM-001");
        row.setPermissionName("재고 조회");
        row.setMenuName("재고관리");
        row.setIsEnabled(1);
        row.setCanRead(1);
        row.setCanWrite(0);
        row.setCanDelete(0);

        RolePermissionMatrixRequest request = new RolePermissionMatrixRequest();
        request.setRoleId("ROLE-002");

        given(roleRepository.findById("ROLE-002")).willReturn(Optional.of(warehouseManagerRole));
        given(rolePermissionQueryMapper.findRolePermissions("ROLE-002")).willReturn(List.of(row));

        RolePermissionMatrixResponse result = roleQueryService.getRolePermissions(request);

        assertThat(result.getRoleId()).isEqualTo("ROLE-002");
        assertThat(result.getRoleName()).isEqualTo(RoleName.WAREHOUSE_MANAGER.name());
        assertThat(result.getPermissions()).hasSize(1);
        assertThat(result.getPermissions().get(0).getPermissionName()).isEqualTo("재고 조회");
    }

    @Test
    @DisplayName("roleName으로 권한 매트릭스 조회 성공")
    void getRolePermissions_byRoleName_success() {
        RolePermissionMatrixRequest request = new RolePermissionMatrixRequest();
        request.setRoleName("WAREHOUSE_WORKER");

        given(roleRepository.findByRoleName(RoleName.WAREHOUSE_WORKER)).willReturn(Optional.of(warehouseWorkerRole));
        given(rolePermissionQueryMapper.findRolePermissions("ROLE-003")).willReturn(List.of());

        RolePermissionMatrixResponse result = roleQueryService.getRolePermissions(request);

        assertThat(result.getRoleName()).isEqualTo(RoleName.WAREHOUSE_WORKER.name());
    }

    @Test
    @DisplayName("roleId/roleName 모두 없으면 예외 발생")
    void getRolePermissions_noParam_throwsException() {
        RolePermissionMatrixRequest request = new RolePermissionMatrixRequest();

        assertThatThrownBy(() -> roleQueryService.getRolePermissions(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    @DisplayName("RBAC 범위 외 역할로 조회 시 예외 발생")
    void getRolePermissions_masterAdmin_throwsRoleScopeRestricted() {
        RolePermissionMatrixRequest request = new RolePermissionMatrixRequest();
        request.setRoleId("ROLE-001");

        given(roleRepository.findById("ROLE-001")).willReturn(Optional.of(masterAdminRole));

        assertThatThrownBy(() -> roleQueryService.getRolePermissions(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_SCOPE_RESTRICTED));
    }

    @Test
    @DisplayName("유효하지 않은 roleName으로 조회 시 예외 발생")
    void getRolePermissions_invalidRoleName_throwsException() {
        RolePermissionMatrixRequest request = new RolePermissionMatrixRequest();
        request.setRoleName("INVALID_ROLE");

        assertThatThrownBy(() -> roleQueryService.getRolePermissions(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    // ─── getRolePermissionHistory ─────────────────────────────────────────────

    @Test
    @DisplayName("권한 변경 이력 조회 성공")
    void getRolePermissionHistory_success() {
        PermissionHistoryResponse item = new PermissionHistoryResponse();
        item.setHistoryId("HIST-001");
        item.setChangedBy("admin@example.com");

        PermissionHistoryRequest request = new PermissionHistoryRequest();
        request.setRoleId("ROLE-002");

        given(roleRepository.findById("ROLE-002")).willReturn(Optional.of(warehouseManagerRole));
        given(rolePermissionQueryMapper.findRolePermissionHistory(request)).willReturn(List.of(item));

        List<PermissionHistoryResponse> result = roleQueryService.getRolePermissionHistory(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHistoryId()).isEqualTo("HIST-001");
        assertThat(result.get(0).getChangedBy()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 역할 이력 조회 - 404 예외 발생")
    void getRolePermissionHistory_roleNotFound_throwsException() {
        PermissionHistoryRequest request = new PermissionHistoryRequest();
        request.setRoleId("ROLE-999");

        given(roleRepository.findById("ROLE-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> roleQueryService.getRolePermissionHistory(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    @DisplayName("RBAC 범위 외 역할 이력 조회 시 예외 발생")
    void getRolePermissionHistory_masterAdmin_throwsRoleScopeRestricted() {
        PermissionHistoryRequest request = new PermissionHistoryRequest();
        request.setRoleId("ROLE-001");

        given(roleRepository.findById("ROLE-001")).willReturn(Optional.of(masterAdminRole));

        assertThatThrownBy(() -> roleQueryService.getRolePermissionHistory(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_SCOPE_RESTRICTED));
    }
}
