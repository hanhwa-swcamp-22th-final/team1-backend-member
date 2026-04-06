package com.conk.member.command.controller;

/*
 * 작업자 직접 발급 컨트롤러 테스트다.
 * TDD에서 로그인 테스트와 분리해서 발급 API 스펙을 독립적으로 검증할 수 있게 넣었다.
 */

import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.application.service.MemberCommandService;
import com.conk.member.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@WebMvcTest(MemberCommandController.class)
@Import(GlobalExceptionHandler.class)
class MemberCommandDirectUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("작업자 직접 발급 API는 ACTIVE 상태를 반환한다")
    void create_direct_user_success() throws Exception {
        MemberResponses.CreateDirectUserResponse response = new MemberResponses.CreateDirectUserResponse();
        response.setId("ACC-201");
        response.setRole("WAREHOUSE_WORKER");
        response.setName("현장작업자1");
        response.setWorkerCode("WORKER-001");
        response.setTenantId("TENANT-001");
        response.setWarehouseId("WH-001");
        response.setAccountStatus("ACTIVE");

        when(memberCommandService.createDirect(any())).thenReturn(response);

        mockMvc.perform(post("/member/users/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId": "TENANT-001",
                                  "warehouseId": "WH-001",
                                  "name": "현장작업자1",
                                  "workerCode": "WORKER-001",
                                  "password": "W0rker!23"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("WAREHOUSE_WORKER"))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }
}
