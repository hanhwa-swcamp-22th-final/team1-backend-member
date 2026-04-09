package com.conk.member.query.controller;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.controller.AdminQueryController;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.service.AdminQueryService;
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

@WebMvcTest(AdminQueryController.class)
class AdminQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminQueryService adminQueryService;

    // ─── GET /member/admin/users ──────────────────────────────────────────────

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공 - 200 OK")
    @WithMockUser
    void getAdminUsers_success_returns200() throws Exception {
        AdminUserListResponse item = new AdminUserListResponse();
        item.setId("ACC-001");
        item.setName("홍길동");
        item.setRole("MASTER_ADMIN");
        item.setStatus("ACTIVE");

        given(adminQueryService.getAdminUsers(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("ACC-001"))
                .andExpect(jsonPath("$.items[0].name").value("홍길동"))
                .andExpect(jsonPath("$.items[0].role").value("MASTER_ADMIN"));
    }

    @Test
    @DisplayName("관리자 사용자 목록 - 결과 없음 200 OK")
    @WithMockUser
    void getAdminUsers_empty_returns200() throws Exception {
        given(adminQueryService.getAdminUsers(any())).willReturn(List.of());

        mockMvc.perform(get("/member/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ─── GET /member/admin/companies ──────────────────────────────────────────

    @Test
    @DisplayName("업체 목록 조회 성공 - 200 OK")
    @WithMockUser
    void getCompanies_success_returns200() throws Exception {
        CompanyListResponse item = new CompanyListResponse();
        item.setId("TENANT-001");
        item.setName("테스트업체");
        item.setStatus("ACTIVE");

        given(adminQueryService.getCompanies(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/admin/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("TENANT-001"))
                .andExpect(jsonPath("$.items[0].name").value("테스트업체"));
    }

    // ─── GET /member/admin/companies/{id} ─────────────────────────────────────

    @Test
    @DisplayName("업체 단건 조회 성공 - 200 OK")
    @WithMockUser
    void getCompany_success_returns200() throws Exception {
        CompanyDetailResponse detail = new CompanyDetailResponse();
        detail.setId("TENANT-001");
        detail.setName("테스트업체");
        detail.setStatus("ACTIVE");
        detail.setSellerCount(3);

        given(adminQueryService.getCompanyById(any())).willReturn(detail);

        mockMvc.perform(get("/member/admin/companies/TENANT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("TENANT-001"))
                .andExpect(jsonPath("$.name").value("테스트업체"))
                .andExpect(jsonPath("$.sellerCount").value(3));
    }

    @Test
    @DisplayName("존재하지 않는 업체 조회 - 404 Not Found")
    @WithMockUser
    void getCompany_notFound_returns404() throws Exception {
        given(adminQueryService.getCompanyById(any()))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(get("/member/admin/companies/TENANT-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
