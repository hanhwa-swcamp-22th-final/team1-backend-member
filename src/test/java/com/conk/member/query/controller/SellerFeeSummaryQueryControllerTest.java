package com.conk.member.query.controller;

import com.conk.member.query.dto.response.SellerFeeSummaryResponse;
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

@WebMvcTest(SellerFeeSummaryQueryController.class)
class SellerFeeSummaryQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SellerQueryService sellerQueryService;

    @Test
    @DisplayName("셀러별 3PL 비용 요약 조회 성공 - 200 OK")
    @WithMockUser
    void getSellerFeeSummary_success_returns200() throws Exception {
        SellerFeeSummaryResponse item = new SellerFeeSummaryResponse();
        item.setSellerCode("SELLER-001");
        item.setSellerName("테스트브랜드");
        item.setEstimatedCost(0.0);

        given(sellerQueryService.getSellerFeeSummary(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/seller/fee-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sellerCode").value("SELLER-001"));
    }
}
