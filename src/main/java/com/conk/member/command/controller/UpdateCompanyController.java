package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.UpdateCompanyRequest;
import com.conk.member.command.application.dto.response.UpdateCompanyResponse;
import com.conk.member.command.application.service.UpdateCompanyCommandService;
import com.conk.member.common.util.AdminPayloadCompat;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateCompanyController {

    private final UpdateCompanyCommandService updateCompanyCommandService;

    public UpdateCompanyController(UpdateCompanyCommandService updateCompanyCommandService) {
        this.updateCompanyCommandService = updateCompanyCommandService;
    }

    @PatchMapping("/member/admin/companies/{id}")
    public Map<String, Object> updateCompany(@PathVariable String id,
                                             @RequestBody UpdateCompanyRequest request) {
        UpdateCompanyResponse response = updateCompanyCommandService.updateCompany(id, request);
        return AdminPayloadCompat.raw(response);
    }
}
