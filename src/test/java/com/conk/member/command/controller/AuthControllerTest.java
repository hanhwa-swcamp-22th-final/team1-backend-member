package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.controller.AuthController;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AuthService authService;

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공 - 200 OK")
    @WithMockUser
    void login_success_returns200() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setToken("access-token");
        response.setRefreshToken("refresh-token");
        response.setId("ACC-001");
        response.setEmail("test@example.com");
        response.setName("테스트유저");
        response.setRole("MASTER_ADMIN");
        response.setStatus("ACTIVE");

        given(authService.login(any())).willReturn(response);

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("emailOrWorkerCode", "test@example.com", "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("MASTER_ADMIN"));
    }

    @Test
    @DisplayName("잘못된 자격증명 - 401 Unauthorized")
    @WithMockUser
    void login_invalidCredentials_returns401() throws Exception {
        given(authService.login(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("emailOrWorkerCode", "wrong@example.com", "password", "wrong")))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비활성화된 계정 로그인 - 403 Forbidden")
    @WithMockUser
    void login_inactiveAccount_returns403() throws Exception {
        given(authService.login(any()))
                .willThrow(new MemberException(ErrorCode.FORBIDDEN));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("emailOrWorkerCode", "inactive@example.com", "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 계정 로그인 - 401 Unauthorized")
    @WithMockUser
    void login_accountNotFound_returns401() throws Exception {
        given(authService.login(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("emailOrWorkerCode", "none@example.com", "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공 - 200 OK")
    @WithMockUser
    void logout_success_returns200() throws Exception {
        willDoNothing().given(authService).logout(any());

        mockMvc.perform(post("/member/auth/logout")
                        .header("Authorization", "Bearer valid-refresh-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("logged out"));
    }

    @Test
    @DisplayName("유효하지 않은 token - 401 Unauthorized")
    @WithMockUser
    void logout_invalidToken_returns401() throws Exception {
        willThrow(new BadCredentialsException("Invalid token"))
                .given(authService).logout(any());

        mockMvc.perform(post("/member/auth/logout")
                        .header("Authorization", "Bearer invalid-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 400 Bad Request")
    @WithMockUser
    void logout_missingAuthorizationHeader_returns400() throws Exception {
        mockMvc.perform(post("/member/auth/logout")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ─── refresh ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("리프레시 토큰으로 새 토큰 발급 - 200 OK")
    @WithMockUser
    void refresh_validToken_returns200() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setToken("new-access-token");
        response.setRefreshToken("new-refresh-token");

        given(authService.refreshToken("valid-refresh-token")).willReturn(response);

        mockMvc.perform(post("/member/auth/refresh")
                        .header("Authorization", "Bearer valid-refresh-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰 - 401 Unauthorized")
    @WithMockUser
    void refresh_invalidToken_returns401() throws Exception {
        given(authService.refreshToken(any()))
                .willThrow(new BadCredentialsException("Refresh Token이 일치하지 않습니다."));

        mockMvc.perform(post("/member/auth/refresh")
                        .header("Authorization", "Bearer invalid-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("만료된 리프레시 토큰 - 401 Unauthorized")
    @WithMockUser
    void refresh_expiredToken_returns401() throws Exception {
        given(authService.refreshToken(any()))
                .willThrow(new BadCredentialsException("Refresh Token이 만료되었습니다."));

        mockMvc.perform(post("/member/auth/refresh")
                        .header("Authorization", "Bearer expired-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("refresh Authorization 헤더 없음 - 400 Bad Request")
    @WithMockUser
    void refresh_missingAuthorizationHeader_returns400() throws Exception {
        mockMvc.perform(post("/member/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ─── setup-password ───────────────────────────────────────────────────────

    @Test
    @DisplayName("최초 비밀번호 설정 성공 - 200 OK")
    @WithMockUser
    void setupPassword_success_returns200() throws Exception {
        SetupPasswordResponse response = new SetupPasswordResponse();
        response.setAccountId("ACC-001");

        given(authService.setupPassword(any())).willReturn(response);

        mockMvc.perform(post("/member/auth/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", "valid-token", "newPassword", "newPass123!")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이미 사용된 설정 토큰 - 409 Conflict")
    @WithMockUser
    void setupPassword_usedToken_returns409() throws Exception {
        given(authService.setupPassword(any()))
                .willThrow(new MemberException(ErrorCode.TOKEN_ALREADY_USED));

        mockMvc.perform(post("/member/auth/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", "used-token", "newPassword", "newPass123!")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("만료된 설정 토큰 - 410 Gone")
    @WithMockUser
    void setupPassword_expiredToken_returns410() throws Exception {
        given(authService.setupPassword(any()))
                .willThrow(new MemberException(ErrorCode.TOKEN_EXPIRED));

        mockMvc.perform(post("/member/auth/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", "expired-token", "newPassword", "newPass123!")))
                        .with(csrf()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── invite ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계정 초대 성공 - 200 OK")
    @WithMockUser
    void invite_success_returns200() throws Exception {
        InviteAccountResponse response = new InviteAccountResponse();
        response.setInvitationId("INV-001");
        response.setEmail("invited@example.com");

        given(authService.invite(any(), any())).willReturn(response);

        mockMvc.perform(post("/member/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "invited@example.com",
                                "roleName", "WAREHOUSE_MANAGER",
                                "tenantId", "TENANT-001")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("중복 이메일 초대 - 409 Conflict")
    @WithMockUser
    void invite_duplicateEmail_returns409() throws Exception {
        given(authService.invite(any(), any()))
                .willThrow(new MemberException(ErrorCode.DUPLICATE_EMAIL));

        mockMvc.perform(post("/member/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "duplicate@example.com",
                                "roleName", "WAREHOUSE_MANAGER",
                                "tenantId", "TENANT-001")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("허용되지 않는 역할 초대 - 403 Forbidden")
    @WithMockUser
    void invite_restrictedRole_returns403() throws Exception {
        given(authService.invite(any(), any()))
                .willThrow(new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED));

        mockMvc.perform(post("/member/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@example.com",
                                "roleName", "WAREHOUSE_WORKER",
                                "tenantId", "TENANT-001")))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
