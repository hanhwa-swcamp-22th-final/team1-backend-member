package com.conk.member.query.controller;

import com.conk.member.query.controller.SellerQueryController;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.service.SellerQueryService;
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

@WebMvcTest(SellerQueryController.class)
class SellerQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SellerQueryService sellerQueryService;

    @Test
    @DisplayName("셀러 목록 조회 성공 - 200 OK")
    @WithMockUser
    void getSellerList_success_returns200() throws Exception {
        SellerListResponse item = new SellerListResponse();
        item.setId("SELLER-001");
        item.setBrandNameKo("테스트브랜드");
        item.setStatus("ACTIVE");
        item.setWarehouseIds(List.of("WH-001", "WH-002"));

        given(sellerQueryService.getSellerList(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("SELLER-001"))
                .andExpect(jsonPath("$.data[0].brandNameKo").value("테스트브랜드"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].warehouseIds[0]").value("WH-001"));
    }

    @Test
    @DisplayName("셀러 목록 - 결과 없음 200 OK")
    @WithMockUser
    void getSellerList_empty_returns200() throws Exception {
        given(sellerQueryService.getSellerList(any())).willReturn(List.of());

        mockMvc.perform(get("/member/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("필터 파라미터로 셀러 목록 조회 - 200 OK")
    @WithMockUser
    void getSellerList_withFilter_returns200() throws Exception {
        SellerListResponse item = new SellerListResponse();
        item.setId("SELLER-001");
        item.setTenantId("TENANT-001");
        item.setStatus("ACTIVE");
        item.setWarehouseIds(List.of());

        given(sellerQueryService.getSellerList(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/sellers")
                        .param("tenantId", "TENANT-001")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenantId").value("TENANT-001"));
    }
}
