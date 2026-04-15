package com.conk.member.query.controller;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminQueryController.class)
class AdminQueryControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  AdminQueryService adminQueryService;

  @Test
  @DisplayName("관리자 사용자 목록 조회 성공 - 200 OK")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getAdminUsers_success_returns200() throws Exception {
    AdminUserListResponse item = new AdminUserListResponse();
    item.setId("ACC-001");
    item.setName("홍길동");
    item.setRole("MASTER_ADMIN");
    item.setStatus("ACTIVE");

    given(adminQueryService.getAdminUsers(any())).willReturn(List.of(item));

    mockMvc.perform(get("/member/admin/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value("ACC-001"))
        .andExpect(jsonPath("$.data[0].name").value("홍길동"))
        .andExpect(jsonPath("$.data[0].role").value("MASTER_ADMIN"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
  }

  @Test
  @DisplayName("관리자 사용자 목록 조회 결과 없음 - 200 OK")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getAdminUsers_empty_returns200() throws Exception {
    given(adminQueryService.getAdminUsers(any())).willReturn(List.of());

    mockMvc.perform(get("/member/admin/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("업체 목록 조회 성공 - 200 OK")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getCompanies_success_returns200() throws Exception {
    CompanyListResponse item = new CompanyListResponse();
    item.setId("TENANT-001");
    item.setName("테스트업체");
    item.setStatus("ACTIVE");

    given(adminQueryService.getCompanies(any())).willReturn(List.of(item));

    mockMvc.perform(get("/member/admin/companies"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value("TENANT-001"))
        .andExpect(jsonPath("$.data[0].name").value("테스트업체"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
  }

  @Test
  @DisplayName("업체 단건 조회 성공 - 200 OK")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getCompany_success_returns200() throws Exception {
    CompanyDetailResponse detail = new CompanyDetailResponse();
    detail.setId("TENANT-001");
    detail.setName("테스트업체");
    detail.setStatus("ACTIVE");
    detail.setSellerCount(3);

    given(adminQueryService.getCompanyById(any())).willReturn(detail);

    mockMvc.perform(get("/member/admin/companies/TENANT-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("TENANT-001"))
        .andExpect(jsonPath("$.data.name").value("테스트업체"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.sellerCount").value(3));
  }

  @Test
  @DisplayName("존재하지 않는 업체 조회 - 404 Not Found")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getCompany_notFound_returns404() throws Exception {
    given(adminQueryService.getCompanyById(any()))
        .willThrow(new MemberException(ErrorCode.NOT_FOUND));

    mockMvc.perform(get("/member/admin/companies/TENANT-999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }


  @Test
  @DisplayName("업체 로그 목록 조회 성공 - 200 OK")
  @WithMockUser(authorities = "SYSTEM_ADMIN")
  void getCompanyLogs_success_returns200() throws Exception {
    com.conk.member.command.application.dto.response.CompanyLogResponse item = new com.conk.member.command.application.dto.response.CompanyLogResponse();
    item.setId("1712990000000");
    item.setCompanyId("TENANT-001");
    item.setActor("sys.admin@conk.com");
    item.setAction("총괄 관리자 추가 발급");

    given(adminQueryService.getCompanyLogs(any())).willReturn(List.of(item));

    mockMvc.perform(get("/member/admin/company-logs").param("companyId", "TENANT-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value("1712990000000"))
        .andExpect(jsonPath("$.data[0].actor").value("sys.admin@conk.com"));
  }

}