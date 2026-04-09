package com.conk.member.query.controller;

import com.conk.member.common.util.AdminPayloadCompat;
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
import java.util.Map;

@RestController
@RequestMapping("/member/admin")
public class AdminQueryController {

    private final AdminQueryService adminQueryService;

    public AdminQueryController(AdminQueryService adminQueryService) {
        this.adminQueryService = adminQueryService;
    }

    @GetMapping("/users")
    public Map<String, Object> getAdminUsers(@RequestParam(required = false, name = "companyId") String companyId,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false, name = "status") String status,
                                             @RequestParam(required = false) String keyword) {
        AdminUserListRequest request = new AdminUserListRequest();
        request.setCompanyId(companyId);
        request.setRole(role);
        request.setStatus(status);
        request.setKeyword(keyword);
        List<AdminUserListResponse> users = adminQueryService.getAdminUsers(request);
        return AdminPayloadCompat.items(users);
    }

    @GetMapping("/companies")
    public Map<String, Object> getCompanies(CompanyListRequest request) {
        List<CompanyListResponse> companies = adminQueryService.getCompanies(request);
        return AdminPayloadCompat.items(companies);
    }

    @GetMapping("/companies/{id}")
    public Map<String, Object> getCompany(@PathVariable String id) {
        CompanyDetailRequest request = new CompanyDetailRequest();
        request.setId(id);
        CompanyDetailResponse company = adminQueryService.getCompanyById(request);
        return AdminPayloadCompat.raw(company);
    }
}
