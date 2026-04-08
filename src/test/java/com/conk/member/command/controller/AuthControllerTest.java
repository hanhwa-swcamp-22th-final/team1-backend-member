package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.InviteAccountCommandService;
import com.conk.member.command.application.service.LoginCommandService;
import com.conk.member.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginCommandService loginCommandService;

    @Mock
    private InviteAccountCommandService inviteAccountCommandService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new LoginController(loginCommandService),
                        new InviteAccountController(inviteAccountCommandService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /member/auth/login 은 토큰과 사용자 정보를 반환한다")
    void login_endpoint_returns_token() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("mock-access-token");
        loginResponse.setId("ACC-001");
        loginResponse.setEmail("admin@conk.com");
        loginResponse.setName("총괄관리자");
        loginResponse.setRole("MASTER_ADMIN");
        loginResponse.setStatus("ACTIVE");
        loginResponse.setTenantId("TENANT-001");

        when(loginCommandService.login(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "emailOrWorkerCode", "admin@conk.com",
                                "password", "P@ssw0rd!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-access-token"))
                .andExpect(jsonPath("$.data.role").value("MASTER_ADMIN"));
    }

    @Test
    @DisplayName("POST /member/auth/invite 는 초대 정보를 반환한다")
    void invite_endpoint_returns_invitation() throws Exception {
        InviteAccountResponse inviteResponse = new InviteAccountResponse();
        inviteResponse.setInvitationId("INV-001");
        inviteResponse.setRole("WAREHOUSE_MANAGER");
        inviteResponse.setEmail("wm@conk.com");
        inviteResponse.setInviteStatus("PENDING");
        inviteResponse.setInviteSentAt(LocalDateTime.now());

        when(inviteAccountCommandService.invite(any(), anyString())).thenReturn(inviteResponse);

        mockMvc.perform(post("/member/auth/invite")
                        .header("X-Invoker-Account-Id", "ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "role", "WAREHOUSE_MANAGER",
                                "tenantId", "TENANT-001",
                                "warehouseId", "WH-001",
                                "name", "창고관리자",
                                "email", "wm@conk.com"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invitationId").value("INV-001"))
                .andExpect(jsonPath("$.data.inviteStatus").value("PENDING"));
    }
}
