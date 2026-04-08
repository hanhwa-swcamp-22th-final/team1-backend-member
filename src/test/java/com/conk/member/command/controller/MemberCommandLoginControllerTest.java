package com.conk.member.command.controller;

/*
 * 로그인 컨트롤러를 슬라이스 테스트로 검증한다.
 * 서비스는 Mock으로 두고, HTTP 요청/응답 스펙이 API 명세서와 맞는지 확인한다.
 */

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.LoginCommandService;
import com.conk.member.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MemberCommandLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginCommandService loginCommandService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("로그인 API는 success/data 래퍼로 응답한다")
    void login_success() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setToken("access-token");
        response.setId("ACC-001");
        response.setEmail("master@conk.com");
        response.setName("시스템 관리자");
        response.setRole("MASTER_ADMIN");
        response.setStatus("ACTIVE");

        when(loginCommandService.login(any())).thenReturn(response);

        mockMvc.perform(post("/member/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailOrWorkerCode": "master@conk.com",
                                  "password": "raw-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("login"))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.role").value("MASTER_ADMIN"));
    }
}
