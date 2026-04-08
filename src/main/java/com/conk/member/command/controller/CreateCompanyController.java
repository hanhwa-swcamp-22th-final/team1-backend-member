package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.CreateCompanyRequest;
import com.conk.member.command.application.dto.response.CreateCompanyResponse;
import com.conk.member.command.application.service.CreateCompanyCommandService;
import com.conk.member.common.util.AdminPayloadCompat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CreateCompanyController {

    private final CreateCompanyCommandService createCompanyCommandService;

    public CreateCompanyController(CreateCompanyCommandService createCompanyCommandService) {
        this.createCompanyCommandService = createCompanyCommandService;
    }

    @PostMapping("/member/admin/companies")
    public Map<String, Object> createCompany(@RequestBody CreateCompanyRequest request) {
        CreateCompanyResponse response = createCompanyCommandService.createCompany(request);
        return AdminPayloadCompat.raw(response);
    }
}
