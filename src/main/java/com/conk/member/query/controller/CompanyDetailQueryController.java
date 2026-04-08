package com.conk.member.query.controller;

import com.conk.member.common.util.AdminPayloadCompat;
import com.conk.member.query.dto.CompanyDetail;
import com.conk.member.query.dto.CompanyDetailRequest;
import com.conk.member.query.service.CompanyDetailQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CompanyDetailQueryController {

    private final CompanyDetailQueryService companyDetailQueryService;

    public CompanyDetailQueryController(CompanyDetailQueryService companyDetailQueryService) {
        this.companyDetailQueryService = companyDetailQueryService;
    }

    @GetMapping("/member/admin/companies/{id}")
    public Map<String, Object> getCompany(@PathVariable String id) {
        CompanyDetailRequest request = new CompanyDetailRequest();
        request.setId(id);
        CompanyDetail company = companyDetailQueryService.getCompanyById(request);
        return AdminPayloadCompat.raw(company);
    }
}
