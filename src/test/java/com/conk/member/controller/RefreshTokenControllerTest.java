package com.conk.member.controller;

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.RefreshTokenCommandService;
import com.conk.member.command.controller.RefreshTokenController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefreshTokenController.class)
class RefreshTokenControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RefreshTokenCommandService refreshTokenCommandService;

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰 발급 - 200 OK")
    @WithMockUser
    void refresh_validToken_returns200() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("new-access-token");
        loginResponse.setRefreshToken("new-refresh-token");
        loginResponse.setId("ACC-001");
        loginResponse.setRole("MASTER_ADMIN");

        given(refreshTokenCommandService.refreshToken("valid-refresh-token")).willReturn(loginResponse);

        mockMvc.perform(post("/member/auth/refresh")
                        .header("Authorization", "Bearer valid-refresh-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰 - 서비스 예외 전파")
    @WithMockUser
    void refresh_invalidToken_serviceThrows() throws Exception {
        given(refreshTokenCommandService.refreshToken(any()))
                .willThrow(new BadCredentialsException("Refresh Token이 일치하지 않습니다."));

        mockMvc.perform(post("/member/auth/refresh")
                        .header("Authorization", "Bearer invalid-token")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
