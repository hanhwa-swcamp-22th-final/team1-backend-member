package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.UpdateCompanyRequest;
import com.conk.member.command.application.dto.response.UpdateCompanyResponse;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.TenantStatus;
import com.conk.member.command.domain.repository.TenantRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UpdateCompanyCommandService {

    private final TenantRepository tenantRepository;

    public UpdateCompanyCommandService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public UpdateCompanyResponse updateCompany(String id, UpdateCompanyRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (StringUtils.hasText(request.getTenantName())) tenant.setTenantName(request.getTenantName());
        if (StringUtils.hasText(request.getRepresentativeName())) tenant.setRepresentativeName(request.getRepresentativeName());
        if (StringUtils.hasText(request.getBusinessNo())) tenant.setBusinessNo(request.getBusinessNo());
        if (StringUtils.hasText(request.getPhoneNo())) tenant.setPhoneNo(request.getPhoneNo());
        if (StringUtils.hasText(request.getEmail())) tenant.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getAddress())) tenant.setAddress(request.getAddress());
        if (StringUtils.hasText(request.getTenantType())) tenant.setTenantType(request.getTenantType());
        if (StringUtils.hasText(request.getStatus())) tenant.setStatus(TenantStatus.valueOf(request.getStatus()));

        tenantRepository.save(tenant);

        UpdateCompanyResponse response = new UpdateCompanyResponse();
        response.setId(tenant.getTenantId());
        response.setTenantCode(tenant.getTenantCode());
        response.setName(tenant.getTenantName());
        response.setStatus(tenant.getStatus().name());
        response.setUpdatedAt(tenant.getUpdatedAt());
        return response;
    }
}
