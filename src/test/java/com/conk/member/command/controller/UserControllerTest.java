package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.UserService;
import com.conk.member.command.application.controller.UserController;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    // ─── createDirect ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("직접 사용자 생성 성공 - 200 OK")
    @WithMockUser
    void createDirect_success_returns200() throws Exception {
        CreateDirectUserResponse response = new CreateDirectUserResponse();
        response.setId("ACC-001");
        response.setRole("WH_WORKER");
        response.setName("홍길동");
        response.setWorkerCode("WC-001");
        response.setAccountStatus("ACTIVE");

        given(userService.createDirect(any())).willReturn(response);

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tenantId", "TENANT-001",
                                "warehouseId", "WH-001",
                                "name", "홍길동",
                                "workerCode", "WC-001",
                                "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ACC-001"))
                .andExpect(jsonPath("$.data.workerCode").value("WC-001"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("중복 작업자 코드 - 409 Conflict")
    @WithMockUser
    void createDirect_duplicateWorkerCode_returns409() throws Exception {
        given(userService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.DUPLICATE_WORKER_CODE));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tenantId", "TENANT-001",
                                "warehouseId", "WH-001",
                                "name", "홍길동",
                                "workerCode", "WC-001",
                                "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("중복 이메일 - 409 Conflict")
    @WithMockUser
    void createDirect_duplicateEmail_returns409() throws Exception {
        given(userService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.DUPLICATE_EMAIL));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tenantId", "TENANT-001",
                                "warehouseId", "WH-001",
                                "name", "홍길동",
                                "workerCode", "WC-002",
                                "password", "password123",
                                "email", "duplicate@example.com")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("유효하지 않은 창고 - 400 Bad Request")
    @WithMockUser
    void createDirect_invalidWarehouse_returns400() throws Exception {
        given(userService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다."));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tenantId", "TENANT-001",
                                "warehouseId", "WH-INVALID",
                                "name", "홍길동",
                                "workerCode", "WC-002",
                                "password", "password123")))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── deactivate ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("사용자 비활성화 성공 - 200 OK")
    @WithMockUser
    void deactivate_success_returns200() throws Exception {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus("INACTIVE");

        given(userService.deactivate("ACC-001")).willReturn(response);

        mockMvc.perform(post("/member/users/ACC-001/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("INACTIVE"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 비활성화 - 404 Not Found")
    @WithMockUser
    void deactivate_userNotFound_returns404() throws Exception {
        given(userService.deactivate(anyString()))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(post("/member/users/ACC-999/deactivate")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("마지막 MASTER_ADMIN 비활성화 시도 - 409 Conflict")
    @WithMockUser
    void deactivate_lastMasterAdmin_returns409() throws Exception {
        given(userService.deactivate(anyString()))
                .willThrow(new MemberException(ErrorCode.LAST_ACTIVE_MASTER_ADMIN_REQUIRED));

        mockMvc.perform(post("/member/users/ACC-001/deactivate")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── reactivate ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("사용자 재활성화 성공 - 200 OK")
    @WithMockUser
    void reactivate_success_returns200() throws Exception {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus("ACTIVE");

        given(userService.reactivate("ACC-001")).willReturn(response);

        mockMvc.perform(post("/member/users/ACC-001/reactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 재활성화 - 404 Not Found")
    @WithMockUser
    void reactivate_userNotFound_returns404() throws Exception {
        given(userService.reactivate(anyString()))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(post("/member/users/ACC-999/reactivate")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── reset-password ───────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 초기화 성공 - 200 OK")
    @WithMockUser
    void resetPassword_success_returns200() throws Exception {
        SimpleUserStatusResponse response = new SimpleUserStatusResponse();
        response.setAccountStatus("TEMP_PASSWORD");
        response.setIsTemporaryPassword(true);

        given(userService.resetPassword("ACC-001")).willReturn(response);

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
        given(userService.resetPassword("ACC-999"))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(post("/member/users/ACC-999/reset-password")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("권한 없는 비밀번호 초기화 - 403 Forbidden")
    @WithMockUser
    void resetPassword_forbidden_returns403() throws Exception {
        given(userService.resetPassword("ACC-001"))
                .willThrow(new MemberException(ErrorCode.FORBIDDEN));

        mockMvc.perform(post("/member/users/ACC-001/reset-password")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
