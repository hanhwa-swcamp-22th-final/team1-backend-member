package com.conk.member.command.repository;

import com.conk.member.command.domain.aggregate.Permission;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.RolePermission;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.PermissionRepository;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RolePermissionRepositoryTest {

    @Autowired RolePermissionRepository rolePermissionRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PermissionRepository permissionRepository;

    private static final String ROLE_ID = "ROLE-002";
    private static final String PERM_ID_1 = "PERM-001";
    private static final String PERM_ID_2 = "PERM-002";

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setRoleId(ROLE_ID);
        role.setRoleName(RoleName.WH_MANAGER);
        role.setRoleDescription("창고관리자");
        role.setCreatedBy("system");
        role.setUpdatedBy("system");
        roleRepository.save(role);

        Permission perm1 = new Permission();
        perm1.setPermissionId(PERM_ID_1);
        perm1.setPermissionName("재고 조회");
        perm1.setMenuName("재고관리");
        perm1.setCreatedBy("system");
        perm1.setUpdatedBy("system");
        permissionRepository.save(perm1);

        Permission perm2 = new Permission();
        perm2.setPermissionId(PERM_ID_2);
        perm2.setPermissionName("입고 처리");
        perm2.setMenuName("입고관리");
        perm2.setCreatedBy("system");
        perm2.setUpdatedBy("system");
        permissionRepository.save(perm2);

        RolePermission rp1 = new RolePermission();
        rp1.setRoleId(ROLE_ID);
        rp1.setPermissionId(PERM_ID_1);
        rp1.setIsEnabled(1);
        rp1.setCanRead(1);
        rp1.setCanWrite(0);
        rp1.setCanDelete(0);
        rp1.setCreatedBy("system");
        rp1.setUpdatedBy("system");
        rolePermissionRepository.save(rp1);

        RolePermission rp2 = new RolePermission();
        rp2.setRoleId(ROLE_ID);
        rp2.setPermissionId(PERM_ID_2);
        rp2.setIsEnabled(1);
        rp2.setCanRead(1);
        rp2.setCanWrite(1);
        rp2.setCanDelete(0);
        rp2.setCreatedBy("system");
        rp2.setUpdatedBy("system");
        rolePermissionRepository.save(rp2);
    }

    @Test
    @DisplayName("역할 ID로 권한 목록 조회")
    void findByRoleId_returnsAllPermissions() {
        List<RolePermission> result = rolePermissionRepository.findByRoleId(ROLE_ID);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(rp -> rp.getRoleId().equals(ROLE_ID));
    }

    @Test
    @DisplayName("존재하지 않는 역할 ID로 조회 시 빈 목록 반환")
    void findByRoleId_notFound_returnsEmpty() {
        List<RolePermission> result = rolePermissionRepository.findByRoleId("ROLE-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("역할 ID + 권한 ID로 단건 조회")
    void findByRoleIdAndPermissionId_success() {
        Optional<RolePermission> result = rolePermissionRepository.findByRoleIdAndPermissionId(ROLE_ID, PERM_ID_1);

        assertThat(result).isPresent();
        assertThat(result.get().getCanRead()).isEqualTo(1);
        assertThat(result.get().getCanWrite()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 조합으로 단건 조회 시 빈 Optional 반환")
    void findByRoleIdAndPermissionId_notFound_returnsEmpty() {
        Optional<RolePermission> result = rolePermissionRepository.findByRoleIdAndPermissionId(ROLE_ID, "PERM-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("권한 값 업데이트 후 저장 확인")
    void update_rolePermission_saved() {
        RolePermission rp = rolePermissionRepository.findByRoleIdAndPermissionId(ROLE_ID, PERM_ID_1).get();
        rp.setCanWrite(1);
        rolePermissionRepository.save(rp);

        RolePermission updated = rolePermissionRepository.findByRoleIdAndPermissionId(ROLE_ID, PERM_ID_1).get();
        assertThat(updated.getCanWrite()).isEqualTo(1);
    }
}
