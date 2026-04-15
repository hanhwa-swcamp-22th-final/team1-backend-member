package com.conk.member.query.controller;

import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerRevenueResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.service.SellerQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$.data[0].warehouseIds[0]").value("WH-001"));
    }

    @Test
    @DisplayName("셀러별 당월 매출 조회 성공 - 200 OK")
    @WithMockUser
    void getSellerRevenue_success_returns200() throws Exception {
        SellerRevenueResponse item = new SellerRevenueResponse();
        item.setSellerCode("SELLER-001");
        item.setSellerName("테스트브랜드");
        item.setMonthRevenue(0.0);

        given(sellerQueryService.getSellerRevenue(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/sellers/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sellerCode").value("SELLER-001"));
    }

    @Test
    @DisplayName("활성 셀러 수 통계 조회 성공 - 200 OK")
    @WithMockUser
    void getSellerStats_success_returns200() throws Exception {
        SellerStatsResponse item = new SellerStatsResponse();
        item.setActiveSellerCount(2);
        item.setInactiveSellerCount(1);
        item.setTotalSellerCount(3);

        given(sellerQueryService.getSellerStats(any())).willReturn(item);

        mockMvc.perform(get("/member/sellers/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeSellerCount").value(2))
                .andExpect(jsonPath("$.data.totalSellerCount").value(3));
    }
}
