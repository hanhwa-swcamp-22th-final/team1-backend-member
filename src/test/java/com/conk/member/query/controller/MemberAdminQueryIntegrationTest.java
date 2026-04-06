package com.conk.member.query.controller;

/*
 * 관리자 화면용 query API를 H2 기반으로 검증한다.
 * 업체 목록과 관리자 사용자 목록이 MyBatis 집계/필터를 통해 내려오는지 확인한다.
 */

import com.conk.member.command.domain.aggregate.*;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MemberAdminQueryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        sellerRepository.deleteAll();
        tenantRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(role("ROLE-MASTER", RoleName.MASTER_ADMIN));
        roleRepository.save(role("ROLE-WM", RoleName.WAREHOUSE_MANAGER));

        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantCode("TEN-001");
        tenant.setTenantName("FASTSHIP LOGISTICS");
        tenant.setRepresentativeName("대표자");
        tenant.setBusinessNo("123-45-67890");
        tenant.setPhoneNo("02-111-2222");
        tenant.setEmail("info@fastship.com");
        tenant.setAddress("Los Angeles");
        tenant.setTenantType("K_GLOBAL");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);

        Seller seller = new Seller();
        seller.setSellerId("SELLER-001");
        seller.setTenantId("TENANT-001");
        seller.setCustomerCode("CUST-001");
        seller.setBrandNameKo("한국미용상사");
        seller.setRepresentativeName("대표자");
        seller.setPhoneNo("010-1111-2222");
        seller.setEmail("ops@kbeauty.com");
        seller.setStatus("ACTIVE");
        sellerRepository.save(seller);

        accountRepository.save(account("ACC-001", "TENANT-001", RoleName.MASTER_ADMIN, AccountStatus.ACTIVE, "총괄관리자", "master@fastship.com"));
        accountRepository.save(account("ACC-002", "TENANT-001", RoleName.WAREHOUSE_MANAGER, AccountStatus.ACTIVE, "창고관리자", "manager@fastship.com"));
    }

    @Test
    @DisplayName("업체 목록 조회 API는 업체와 seller/user 집계값을 반환한다")
    void get_companies_success() throws Exception {
        mockMvc.perform(get("/member/admin/companies").param("keyword", "FASTSHIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("FASTSHIP LOGISTICS"))
                .andExpect(jsonPath("$.items[0].tenantCode").value("TEN-001"))
                .andExpect(jsonPath("$.items[0].sellerCount").value(1))
                .andExpect(jsonPath("$.items[0].userCount").value(2));
    }

    @Test
    @DisplayName("관리자 사용자 목록 조회 API는 companyId와 role 필터를 적용한다")
    void get_admin_users_success() throws Exception {
        mockMvc.perform(get("/member/admin/users")
                        .param("companyId", "TENANT-001")
                        .param("role", "MASTER_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].tenantId").value("TENANT-001"))
                .andExpect(jsonPath("$.items[0].role").value("MASTER_ADMIN"))
                .andExpect(jsonPath("$.items[0].email").value("master@fastship.com"));
    }

    private Role role(String roleId, RoleName roleName) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName(roleName);
        role.setRoleDescription(roleName.name());
        return role;
    }

    private Account account(String accountId, String tenantId, RoleName roleName, AccountStatus status, String name, String email) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole(roleRepository.findByRoleName(roleName).orElseThrow());
        account.setTenantId(tenantId);
        account.setAccountName(name);
        account.setEmail(email);
        account.setAccountStatus(status);
        account.setIsTemporaryPassword(Boolean.FALSE);
        return account;
    }
}
