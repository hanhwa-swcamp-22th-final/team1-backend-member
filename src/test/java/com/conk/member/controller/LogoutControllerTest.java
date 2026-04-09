package com.conk.member.controller;

import com.conk.member.command.application.service.LogoutCommandService;
import com.conk.member.command.controller.LogoutController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogoutController.class)
class LogoutControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean LogoutCommandService logoutCommandService;

    @Test
    @DisplayName("로그아웃 성공 - 200 OK 반환")
    @WithMockUser
    void logout_success_returns200() throws Exception {
        willDoNothing().given(logoutCommandService).logout(anyString());

        mockMvc.perform(post("/member/auth/logout")
                        .header("Authorization", "Bearer valid-refresh-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("logged out"));
    }

    @Test
    @DisplayName("유효하지 않은 token - 서비스 예외 전파")
    @WithMockUser
    void logout_invalidToken_serviceThrows() throws Exception {
        willThrow(new BadCredentialsException("Invalid token"))
                .given(logoutCommandService).logout(anyString());

        mockMvc.perform(post("/member/auth/logout")
                        .header("Authorization", "Bearer invalid-token")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authorization 헤더 없음 - 400 Bad Request")
    @WithMockUser
    void logout_missingAuthorizationHeader_returns400() throws Exception {
        mockMvc.perform(post("/member/auth/logout")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
