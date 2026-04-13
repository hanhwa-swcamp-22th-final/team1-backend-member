package com.conk.member.command.controller;

import com.conk.member.command.application.controller.AuthController;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.security.MemberUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AuthService authService;

    private UsernamePasswordAuthenticationToken memberAuthentication(String accountId) {
        MemberUserPrincipal principal = new MemberUserPrincipal(
                accountId,
                "테스트유저",
                "SELLER-001",
                "TENANT-001",
                "MASTER_ADMIN",
                "",
                java.util.List.of(new SimpleGrantedAuthority("MASTER_ADMIN"))
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("로그인 성공 - refresh token 쿠키와 함께 200 OK")
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
        given(authService.getRefreshExpiration()).willReturn(604800000L);

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("emailOrWorkerCode", "test@example.com", "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", "refresh-token"))
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

    @Test
    @DisplayName("로그아웃 성공 - 쿠키 삭제와 함께 200 OK")
    void logout_success_returns200() throws Exception {
        willDoNothing().given(authService).logout("ACC-001");

        mockMvc.perform(post("/member/auth/logout")
                        .with(authentication(memberAuthentication("ACC-001")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("logged out"));
    }

    @Test
    @DisplayName("인증 없이 로그아웃 요청 - 401 Unauthorized")
    void logout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/member/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키로 새 토큰 발급 - 200 OK")
    @WithMockUser
    void refresh_validToken_returns200() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setToken("new-access-token");
        response.setRefreshToken("new-refresh-token");

        given(authService.refreshToken("valid-refresh-token")).willReturn(response);
        given(authService.getRefreshExpiration()).willReturn(604800000L);

        mockMvc.perform(post("/member/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", "new-refresh-token"))
                .andExpect(jsonPath("$.data.token").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰 - 401 Unauthorized")
    @WithMockUser
    void refresh_invalidToken_returns401() throws Exception {
        given(authService.refreshToken(any()))
                .willThrow(new BadCredentialsException("Refresh Token이 일치하지 않습니다."));
        given(authService.getRefreshExpiration()).willReturn(604800000L);

        mockMvc.perform(post("/member/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token"))
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
        given(authService.getRefreshExpiration()).willReturn(604800000L);

        mockMvc.perform(post("/member/auth/refresh")
                        .cookie(new Cookie("refreshToken", "expired-token"))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("refresh token 쿠키가 없으면 401 Unauthorized")
    @WithMockUser
    void refresh_missingCookie_returns401() throws Exception {
        mockMvc.perform(post("/member/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

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
                                Map.of("setupToken", "valid-token", "newPassword", "newPass123!")))
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
                                Map.of("setupToken", "used-token", "newPassword", "newPass123!")))
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
                                Map.of("setupToken", "expired-token", "newPassword", "newPass123!")))
                        .with(csrf()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false));
    }
}
