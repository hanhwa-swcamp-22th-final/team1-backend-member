package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.command.application.dto.response.CompanyLogResponse;
import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.CompanyLogListRequest;
import com.conk.member.query.dto.request.CompanyDetailRequest;
import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.service.AdminQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/member/admin")
public class AdminQueryController {

  private final AdminQueryService adminQueryService;

  public AdminQueryController(AdminQueryService adminQueryService) {
    this.adminQueryService = adminQueryService;
  }

  @GetMapping("/users")
  public ResponseEntity<ApiResponse<List<AdminUserListResponse>>> getAdminUsers(
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
    return ResponseEntity.ok(ApiResponse.ok("admin user list", users));
  }

  @GetMapping("/companies")
  public ResponseEntity<ApiResponse<List<CompanyListResponse>>> getCompanies(CompanyListRequest request) {
    List<CompanyListResponse> companies = adminQueryService.getCompanies(request);
    return ResponseEntity.ok(ApiResponse.ok("company list", companies));
  }

  @GetMapping("/company-logs")
  public ResponseEntity<ApiResponse<List<CompanyLogResponse>>> getCompanyLogs(
      @RequestParam(required = false, name = "companyId") String companyId,
      @RequestParam(required = false) String actor,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
  ) {
    CompanyLogListRequest request = new CompanyLogListRequest();
    request.setCompanyId(companyId);
    request.setActor(actor);
    request.setAction(action);
    request.setFrom(from);
    request.setTo(to);

    return ResponseEntity.ok(ApiResponse.ok("company logs", adminQueryService.getCompanyLogs(request)));
  }

  @GetMapping("/companies/{id}")
  public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompany(@PathVariable String id) {
    CompanyDetailRequest request = new CompanyDetailRequest();
    request.setId(id);

    CompanyDetailResponse company = adminQueryService.getCompanyById(request);
    return ResponseEntity.ok(ApiResponse.ok("company detail", company));
  }
}