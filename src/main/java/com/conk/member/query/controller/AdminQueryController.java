package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.CompanyDetailRequest;
import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.service.AdminQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/member/admin")
public class AdminQueryController {

  private final AdminQueryService adminQueryService;

  public AdminQueryController(AdminQueryService adminQueryService) {
    this.adminQueryService = adminQueryService;
  }

  @GetMapping("/users")
  public ApiResponse<List<AdminUserListResponse>> getAdminUsers(
      @RequestParam(required = false, name = "companyId") String companyId,
      @RequestParam(required = false) String role,
      @RequestParam(required = false, name = "status") String status,
      @RequestParam(required = false) String keyword
  ) {
    AdminUserListRequest request = new AdminUserListRequest();
    request.setCompanyId(companyId);
    request.setRole(role);
    request.setStatus(status);
    request.setKeyword(keyword);

    List<AdminUserListResponse> users = adminQueryService.getAdminUsers(request);
    return ApiResponse.ok("admin user list", users);
  }

  @GetMapping("/companies")
  public ApiResponse<List<CompanyListResponse>> getCompanies(CompanyListRequest request) {
    List<CompanyListResponse> companies = adminQueryService.getCompanies(request);
    return ApiResponse.ok("company list", companies);
  }

  @GetMapping("/companies/{id}")
  public ApiResponse<CompanyDetailResponse> getCompany(@PathVariable String id) {
    CompanyDetailRequest request = new CompanyDetailRequest();
    request.setId(id);

    CompanyDetailResponse company = adminQueryService.getCompanyById(request);
    return ApiResponse.ok("company detail", company);
  }
}