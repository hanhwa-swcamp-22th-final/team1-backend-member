package com.conk.member.controller;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.ResetPasswordCommandService;
import com.conk.member.command.controller.ResetPasswordController;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResetPasswordController.class)
class ResetPasswordControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ResetPasswordCommandService resetPasswordCommandService;

    @Test
    @DisplayName("비밀번호 초기화 성공 - 200 OK")
    @WithMockUser
    void resetPassword_success_returns200() throws Exception {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus("TEMP_PASSWORD");
        response.setIsTemporaryPassword(true);

        given(resetPasswordCommandService.resetPassword("ACC-001")).willReturn(response);

        mockMvc.perform(post("/member/users/ACC-001/reset-password")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("TEMP_PASSWORD"))
                .andExpect(jsonPath("$.data.isTemporaryPassword").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 비밀번호 초기화 - 404 Not Found")
    @WithMockUser
    void resetPassword_userNotFound_returns404() throws Exception {
        given(resetPasswordCommandService.resetPassword("ACC-999"))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(post("/member/users/ACC-999/reset-password")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
