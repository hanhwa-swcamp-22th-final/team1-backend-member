package com.conk.member.command.controller;

/*
 * 로그인과 작업자 직접 발급 API를 통합 테스트로 검증한다.
 */

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MemberCommandControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        roleRepository.deleteAll();
        Role master = new Role(); master.setRoleId("ROLE-001"); master.setRoleName(RoleName.MASTER_ADMIN); master.setRoleDescription("master");
        Role worker = new Role(); worker.setRoleId("ROLE-002"); worker.setRoleName(RoleName.WAREHOUSE_WORKER); worker.setRoleDescription("worker");
        roleRepository.save(master); roleRepository.save(worker);
        Account account = new Account();
        account.setAccountId("ACC-001"); account.setRole(master); account.setTenantId("TENANT-001"); account.setAccountName("관리자"); account.setEmail("master@conk.com"); account.setPasswordHash(new BCryptPasswordEncoder().encode("raw-password")); account.setAccountStatus(AccountStatus.ACTIVE); account.setIsTemporaryPassword(Boolean.FALSE);
        accountRepository.save(account);
    }

    @Test
    @DisplayName("로그인 API는 토큰을 반환한다")
    void login_api_success() throws Exception {
        String body = """
                {
                  "emailOrWorkerCode": "master@conk.com",
                  "password": "raw-password"
                }
                """;
        mockMvc.perform(post("/member/auth/login").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("작업자 직접 발급 API는 ACTIVE 상태로 생성한다")
    void create_direct_api_success() throws Exception {
        String body = """
                {
                  "tenantId": "TENANT-001",
                  "warehouseId": "WH-001",
                  "name": "작업자",
                  "workerCode": "WORKER-001",
                  "password": "W0rker!23"
                }
                """;
        mockMvc.perform(post("/member/users/direct").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("WAREHOUSE_WORKER"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }
}
