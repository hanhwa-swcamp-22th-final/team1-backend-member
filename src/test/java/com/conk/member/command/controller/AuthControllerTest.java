package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.AuthService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
        .setValidator(validator)
        .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("로그인 응답은 success message data 구조로 반환된다")
  void login_returns_wrapped_response() throws Exception {
    LoginResponse response = new LoginResponse(
        1L,
        "시스템 관리자",
        "sys.admin@conk.com",
        "WORKER-001",
        "SYSTEM_ADMIN",
        "ACTIVE",
        "CONK 본사",
        "mock-token-sys"
    );

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/member/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new LoginRequest("sys.admin@conk.com", "password"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("login"))
        .andExpect(jsonPath("$.data.token").value("mock-token-sys"))
        .andExpect(jsonPath("$.data.user.id").value(1))
        .andExpect(jsonPath("$.data.user.email").value("sys.admin@conk.com"))
        .andExpect(jsonPath("$.data.user.name").value("시스템 관리자"))
        .andExpect(jsonPath("$.data.user.role").value("SYSTEM_ADMIN"))
        .andExpect(jsonPath("$.data.user.status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.user.organization").value("CONK 본사"));

    verify(authService).login(any(LoginRequest.class));
  }



}
