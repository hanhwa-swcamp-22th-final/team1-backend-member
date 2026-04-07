package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CompanyAdminQueryController {

    private final MemberQueryService memberQueryService;

    public CompanyAdminQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
    }

    @GetMapping("/member/admin/companies")
    public ApiResponse<List<QueryResponses.CompanySummary>> getCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        return ApiResponse.ok("company list", memberQueryService.getCompanies(keyword, status));
    }

    @GetMapping("/member/admin/companies/{id}")
    public ApiResponse<QueryResponses.CompanyDetail> getCompany(@PathVariable String id) {
        return ApiResponse.ok("company detail", memberQueryService.getCompanyById(id));
    }
}
