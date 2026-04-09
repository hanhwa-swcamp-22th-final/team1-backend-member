package com.conk.member.controller;

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.LoginCommandService;
import com.conk.member.command.controller.LoginController;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean LoginCommandService loginCommandService;

    @Test
    @DisplayName("로그인 성공 - 200 OK와 토큰 반환")
    @WithMockUser
    void login_success_returns200() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setId("ACC-001");
        loginResponse.setEmail("test@example.com");
        loginResponse.setName("테스트유저");
        loginResponse.setRole("MASTER_ADMIN");
        loginResponse.setStatus("ACTIVE");

        given(loginCommandService.login(any())).willReturn(loginResponse);

        String requestBody = objectMapper.writeValueAsString(
                Map.of("emailOrWorkerCode", "test@example.com", "password", "password123"));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("MASTER_ADMIN"));
    }

    @Test
    @DisplayName("잘못된 자격증명 - 401 Unauthorized")
    @WithMockUser
    void login_invalidCredentials_returns401() throws Exception {
        given(loginCommandService.login(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_CREDENTIALS));

        String requestBody = objectMapper.writeValueAsString(
                Map.of("emailOrWorkerCode", "wrong@example.com", "password", "wrong"));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비활성화된 계정 로그인 - 403 Forbidden")
    @WithMockUser
    void login_inactiveAccount_returns403() throws Exception {
        given(loginCommandService.login(any()))
                .willThrow(new MemberException(ErrorCode.FORBIDDEN));

        String requestBody = objectMapper.writeValueAsString(
                Map.of("emailOrWorkerCode", "inactive@example.com", "password", "password123"));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 계정 로그인 - 401 Unauthorized")
    @WithMockUser
    void login_accountNotFound_returns401() throws Exception {
        given(loginCommandService.login(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_CREDENTIALS));

        String requestBody = objectMapper.writeValueAsString(
                Map.of("emailOrWorkerCode", "none@example.com", "password", "password123"));

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
