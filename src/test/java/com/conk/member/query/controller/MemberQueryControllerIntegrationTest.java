package com.conk.member.query.controller;

/*
 * 셀러 목록 조회와 RBAC 권한 매트릭스 조회를 통합 테스트로 검증한다.
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
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS permission (permission_id VARCHAR(255) PRIMARY KEY, permission_name VARCHAR(255), permission_group VARCHAR(255))");
        jdbcTemplate.update("DELETE FROM permission");
        jdbcTemplate.update("INSERT INTO permission(permission_id, permission_name, permission_group) VALUES (?,?,?)", "PERM-001", "입고관리", "WAREHOUSE");
        rolePermissionRepository.deleteAll();
        sellerRepository.deleteAll();
        roleRepository.deleteAll();
        Seller seller = new Seller();
        seller.setSellerId("SELLER-001"); seller.setTenantId("TENANT-001"); seller.setBrandNameKo("한국미용상사"); seller.setRepresentativeName("김미영"); seller.setPhoneNo("010-1111-2222"); seller.setEmail("ops@kbeauty.com"); seller.setStatus("ACTIVE"); seller.setCustomerCode("CUST-001");
        sellerRepository.save(seller);
        Role role = new Role(); role.setRoleId("ROLE-100"); role.setRoleName(RoleName.WAREHOUSE_MANAGER); role.setRoleDescription("manager");
        roleRepository.save(role);
        RolePermission rp = new RolePermission(); rp.setRoleId("ROLE-100"); rp.setPermissionId("PERM-001"); rp.setIsEnabled(1); rp.setCanRead(1); rp.setCanWrite(1); rp.setCanDelete(0);
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
    @DisplayName("RBAC 매트릭스 조회 API는 권한 리스트를 반환한다")
    void role_permission_matrix_success() throws Exception {
        mockMvc.perform(get("/member/roles/permissions").param("roleId", "ROLE-100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleName").value("WAREHOUSE_MANAGER"));
    }
}
