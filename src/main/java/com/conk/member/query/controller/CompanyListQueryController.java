package com.conk.member.query.controller;

import com.conk.member.common.util.AdminPayloadCompat;
import com.conk.member.query.dto.CompanySummary;
import com.conk.member.query.dto.CompanyListRequest;
import com.conk.member.query.service.CompanyListQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CompanyListQueryController {

    private final CompanyListQueryService companyListQueryService;

    public CompanyListQueryController(CompanyListQueryService companyListQueryService) {
        this.companyListQueryService = companyListQueryService;
    }

    @GetMapping("/member/admin/companies")
    public Map<String, Object> getCompanies(CompanyListRequest request) {
        List<CompanySummary> companies = companyListQueryService.getCompanies(request);
        return AdminPayloadCompat.items(companies);
    }
}
