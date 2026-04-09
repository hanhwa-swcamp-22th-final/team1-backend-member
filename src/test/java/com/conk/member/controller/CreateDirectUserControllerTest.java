package com.conk.member.controller;

import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.service.CreateDirectUserCommandService;
import com.conk.member.command.controller.CreateDirectUserController;
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

@WebMvcTest(CreateDirectUserController.class)
class CreateDirectUserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CreateDirectUserCommandService createDirectUserCommandService;

    @Test
    @DisplayName("직접 사용자 생성 성공 - 200 OK")
    @WithMockUser
    void createDirect_success_returns200() throws Exception {
        CreateDirectUserResponse response = new CreateDirectUserResponse();
        response.setId("ACC-001");
        response.setRole("WAREHOUSE_WORKER");
        response.setName("홍길동");
        response.setWorkerCode("WC-001");
        response.setTenantId("TENANT-001");
        response.setWarehouseId("WH-001");
        response.setAccountStatus("ACTIVE");

        given(createDirectUserCommandService.createDirect(any())).willReturn(response);

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "tenantId", "TENANT-001",
                "warehouseId", "WH-001",
                "name", "홍길동",
                "workerCode", "WC-001",
                "password", "password123",
                "email", "worker@example.com"
        ));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ACC-001"))
                .andExpect(jsonPath("$.data.role").value("WAREHOUSE_WORKER"))
                .andExpect(jsonPath("$.data.workerCode").value("WC-001"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("중복 작업자 코드 - 409 Conflict")
    @WithMockUser
    void createDirect_duplicateWorkerCode_returns409() throws Exception {
        given(createDirectUserCommandService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.DUPLICATE_WORKER_CODE));

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "tenantId", "TENANT-001",
                "warehouseId", "WH-001",
                "name", "홍길동",
                "workerCode", "WC-001",
                "password", "password123"
        ));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("유효하지 않은 창고 - 400 Bad Request")
    @WithMockUser
    void createDirect_invalidWarehouse_returns400() throws Exception {
        given(createDirectUserCommandService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다."));

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "tenantId", "TENANT-001",
                "warehouseId", "WH-INVALID",
                "name", "홍길동",
                "workerCode", "WC-002",
                "password", "password123"
        ));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("중복 이메일 - 409 Conflict")
    @WithMockUser
    void createDirect_duplicateEmail_returns409() throws Exception {
        given(createDirectUserCommandService.createDirect(any()))
                .willThrow(new MemberException(ErrorCode.DUPLICATE_EMAIL));

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "tenantId", "TENANT-001",
                "warehouseId", "WH-001",
                "name", "홍길동",
                "workerCode", "WC-003",
                "password", "password123",
                "email", "duplicate@example.com"
        ));

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
