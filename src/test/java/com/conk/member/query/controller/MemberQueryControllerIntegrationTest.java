package com.conk.member.query.controller;

/*
 * 셀러 목록 조회와 RBAC 권한 매트릭스 조회를 통합 테스트로 검증한다.
 * 수정: getRolePermissions 이제 List 반환 → data[0].roleName 으로 검증.
 *       permission 테이블 컬럼을 실제 엔티티(menu_name) 기준으로 수정.
 */

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.RolePermission;
import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MemberQueryControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // permission 테이블: 실제 엔티티 컬럼(menu_name) 기준
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS permission (" +
            "  permission_id VARCHAR(255) PRIMARY KEY," +
            "  permission_name VARCHAR(255) NOT NULL," +
            "  menu_name VARCHAR(255)," +
            "  permission_description VARCHAR(255)," +
            "  created_at TIMESTAMP," +
            "  created_by VARCHAR(255)," +
            "  updated_at TIMESTAMP," +
            "  updated_by VARCHAR(255))"
        );
        jdbcTemplate.update("DELETE FROM role_permission_history");
        jdbcTemplate.update("DELETE FROM permission");
        jdbcTemplate.update(
            "INSERT INTO permission(permission_id, permission_name, menu_name) VALUES (?,?,?)",
            "PERM-001", "입고관리", "inbound"
        );
        rolePermissionRepository.deleteAll();
        sellerRepository.deleteAll();
        roleRepository.deleteAll();

        Seller seller = new Seller();
        seller.setSellerId("SELLER-001");
        seller.setTenantId("TENANT-001");
        seller.setBrandNameKo("한국미용상사");
        seller.setRepresentativeName("김미영");
        seller.setPhoneNo("010-1111-2222");
        seller.setEmail("ops@kbeauty.com");
        seller.setStatus("ACTIVE");
        seller.setCustomerCode("CUST-001");
        sellerRepository.save(seller);

        Role role = new Role();
        role.setRoleId("ROLE-100");
        role.setRoleName(RoleName.WAREHOUSE_MANAGER);
        role.setRoleDescription("manager");
        roleRepository.save(role);

        RolePermission rp = new RolePermission();
        rp.setRoleId("ROLE-100");
        rp.setPermissionId("PERM-001");
        rp.setIsEnabled(1);
        rp.setCanRead(1);
        rp.setCanWrite(1);
        rp.setCanDelete(0);
        rolePermissionRepository.save(rp);
    }

    @Test
    @DisplayName("셀러 목록 조회 API는 리스트를 반환한다")
    void seller_list_success() throws Exception {
        mockMvc.perform(get("/member/sellers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].customerCode").value("CUST-001"));
    }

  @Test
  @DisplayName("역할별 권한 매트릭스를 조회할 수 있다")
  void role_permission_matrix_success() throws Exception {
    mockMvc.perform(get("/member/roles/permissions")
            .param("roleId", "ROLE-100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("role permission matrix"))
        .andExpect(jsonPath("$.data.roleId").value("ROLE-100"))
        .andExpect(jsonPath("$.data.roleName").value("WAREHOUSE_MANAGER"))
        .andExpect(jsonPath("$.data.permissions[0].permissionId").value("PERM-001"))
        .andExpect(jsonPath("$.data.permissions[0].permissionName").value("입고관리"))
        .andExpect(jsonPath("$.data.permissions[0].isEnabled").value(1))
        .andExpect(jsonPath("$.data.permissions[0].canRead").value(1))
        .andExpect(jsonPath("$.data.permissions[0].canWrite").value(1))
        .andExpect(jsonPath("$.data.permissions[0].canDelete").value(0));
  }

}
