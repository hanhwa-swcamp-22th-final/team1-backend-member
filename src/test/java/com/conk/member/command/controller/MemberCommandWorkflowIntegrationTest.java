package com.conk.member.command.controller;

/*
 * H2 기반으로 주요 command API 흐름을 묶어서 검증한다.
 * 업체 등록, 최초 비밀번호 설정, 초대, 계정 상태 전환 같은 핵심 시나리오를 본다.
 */

import com.conk.member.command.domain.aggregate.*;
import com.conk.member.command.domain.enums.*;
import com.conk.member.command.domain.repository.*;
import com.conk.member.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MemberCommandWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private MemberTokenRepository memberTokenRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        invitationRepository.deleteAll();
        memberTokenRepository.deleteAll();
        accountRepository.deleteAll();
        sellerRepository.deleteAll();
        tenantRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(role("ROLE-SYS", RoleName.SYSTEM_ADMIN));
        roleRepository.save(role("ROLE-MASTER", RoleName.MASTER_ADMIN));
        roleRepository.save(role("ROLE-WM", RoleName.WAREHOUSE_MANAGER));
        roleRepository.save(role("ROLE-WORKER", RoleName.WAREHOUSE_WORKER));
        roleRepository.save(role("ROLE-SELLER", RoleName.SELLER));
    }

    @Test
    @DisplayName("업체 등록 API는 tenant와 최초 총괄관리자를 생성한다")
    void create_company_workflow_success() throws Exception {
        String body = """
                {
                  "tenantName": "FASTSHIP LOGISTICS",
                  "representativeName": "대표자",
                  "businessNo": "123-45-67890",
                  "phoneNo": "02-111-2222",
                  "email": "info@fastship.com",
                  "address": "Los Angeles",
                  "tenantType": "K_GLOBAL",
                  "masterAdminName": "총괄관리자",
                  "masterAdminEmail": "master@fastship.com"
                }
                """;

        mockMvc.perform(post("/member/admin/companies")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("FASTSHIP LOGISTICS"))
                .andExpect(jsonPath("$.data.status").value("SETTING"))
                .andExpect(jsonPath("$.data.masterAdminEmail").value("master@fastship.com"));

        assertThat(tenantRepository.findAll()).hasSize(1);
        assertThat(accountRepository.findAll()).hasSize(1);
        assertThat(memberTokenRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("최초 비밀번호 설정 API는 총괄관리자와 업체를 함께 활성화한다")
    void setup_password_workflow_success() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setTenantId("TENANT-001");
        tenant.setTenantCode("TEN-001");
        tenant.setTenantName("FASTSHIP LOGISTICS");
        tenant.setStatus(TenantStatus.SETTING);
        tenantRepository.save(tenant);

        Account account = new Account();
        account.setAccountId("ACC-001");
        account.setRole(roleRepository.findByRoleName(RoleName.MASTER_ADMIN).orElseThrow());
        account.setTenantId("TENANT-001");
        account.setAccountName("총괄관리자");
        account.setEmail("master@fastship.com");
        account.setAccountStatus(AccountStatus.TEMP_PASSWORD);
        account.setIsTemporaryPassword(Boolean.TRUE);
        accountRepository.save(account);

        String rawToken = "setup-token-value";
        MemberToken token = new MemberToken();
        token.setTokenId("TOK-001");
        token.setAccountId("ACC-001");
        // JwtTokenProvider는 내부 hashToken() 을 사용하므로 여기서도 SHA-256 직접 계산
        String hashedToken;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            hashedToken = java.util.Base64.getEncoder().encodeToString(
                digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
        token.setTokenHash(hashedToken);
        token.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setIsUsed(Boolean.FALSE);
        memberTokenRepository.save(token);

        String body = """
                {
                  "setupToken": "setup-token-value",
                  "newPassword": "NewP@ssw0rd!"
                }
                """;

        mockMvc.perform(post("/member/auth/setup-password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.tenantStatus").value("ACTIVE"));

        Account savedAccount = accountRepository.findById("ACC-001").orElseThrow();
        Tenant savedTenant = tenantRepository.findById("TENANT-001").orElseThrow();
        MemberToken savedToken = memberTokenRepository.findById("TOK-001").orElseThrow();
        assertThat(savedAccount.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(savedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(savedToken.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("이메일 초대 API는 invitation을 생성하고 PENDING 상태를 반환한다")
    void invite_account_workflow_success() throws Exception {
        sellerRepository.save(seller("SELLER-001", "TENANT-001", "CUST-001", "한국미용상사"));

        String body = """
                {
                  "role": "SELLER",
                  "tenantId": "TENANT-001",
                  "sellerId": "SELLER-001",
                  "name": "셀러담당자",
                  "email": "seller.user@conk.com"
                }
                """;

        mockMvc.perform(post("/member/auth/invite")
                        .header("X-Invoker-Account-Id", "ACC-INVOKER")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("SELLER"))
                .andExpect(jsonPath("$.data.inviteStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.sellerId").value("SELLER-001"));

        assertThat(invitationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("비활성화 후 재활성화 API는 계정 상태를 INACTIVE에서 ACTIVE로 되돌린다")
    void deactivate_then_reactivate_success() throws Exception {
        Account account = new Account();
        account.setAccountId("ACC-USER-001");
        account.setRole(roleRepository.findByRoleName(RoleName.WAREHOUSE_WORKER).orElseThrow());
        account.setTenantId("TENANT-001");
        account.setWarehouseId("WH-001");
        account.setAccountName("작업자");
        account.setWorkerCode("WORKER-001");
        account.setPasswordHash(new BCryptPasswordEncoder().encode("raw-password"));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);

        mockMvc.perform(post("/member/users/ACC-USER-001/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus").value("INACTIVE"));

        mockMvc.perform(post("/member/users/ACC-USER-001/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    private Role role(String roleId, RoleName roleName) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName(roleName);
        role.setRoleDescription(roleName.name());
        return role;
    }

    private Seller seller(String sellerId, String tenantId, String customerCode, String brandNameKo) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setTenantId(tenantId);
        seller.setCustomerCode(customerCode);
        seller.setBrandNameKo(brandNameKo);
        seller.setRepresentativeName("대표자");
        seller.setPhoneNo("010-1111-2222");
        seller.setEmail("ops@kbeauty.com");
        seller.setStatus("ACTIVE");
        return seller;
    }
}
