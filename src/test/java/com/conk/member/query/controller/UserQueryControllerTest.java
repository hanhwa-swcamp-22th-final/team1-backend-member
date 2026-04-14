package com.conk.member.query.controller;

import com.conk.member.query.controller.UserQueryController;
import com.conk.member.query.dto.response.UserListResponse;
import com.conk.member.query.service.UserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserQueryController.class)
class UserQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserQueryService userQueryService;

    @Test
    @DisplayName("사용자 목록 조회 성공 - 200 OK")
    @WithMockUser
    void getUsers_success_returns200() throws Exception {
        UserListResponse item = new UserListResponse();
        item.setId("ACC-001");
        item.setName("홍길동");
        item.setRole("WH_WORKER");
        item.setAccountStatus("ACTIVE");

        given(userQueryService.getUsers(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("ACC-001"))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data[0].role").value("WH_WORKER"))
                .andExpect(jsonPath("$.data[0].accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("사용자 목록 - 결과 없음 200 OK")
    @WithMockUser
    void getUsers_empty_returns200() throws Exception {
        given(userQueryService.getUsers(any())).willReturn(List.of());

        mockMvc.perform(get("/member/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("필터 파라미터로 사용자 목록 조회 - 200 OK")
    @WithMockUser
    void getUsers_withFilter_returns200() throws Exception {
        UserListResponse item = new UserListResponse();
        item.setId("ACC-001");
        item.setRole("WH_WORKER");
        item.setAccountStatus("ACTIVE");
        item.setWarehouseId("WH-001");

        given(userQueryService.getUsers(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/users")
                        .param("role", "WH_WORKER")
                        .param("accountStatus", "ACTIVE")
                        .param("warehouseId", "WH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].warehouseId").value("WH-001"));
    }
}
