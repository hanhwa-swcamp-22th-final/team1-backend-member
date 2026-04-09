package com.conk.member.query.service;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.CompanyDetailRequest;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.mapper.CompanyQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CompanyDetailQueryService {

    private final CompanyQueryMapper companyQueryMapper;

    public CompanyDetailQueryService(CompanyQueryMapper companyQueryMapper) {
        this.companyQueryMapper = companyQueryMapper;
    }

    public CompanyDetailResponse getCompanyById(CompanyDetailRequest request) {
        CompanyDetailResponse item = companyQueryMapper.findCompanyById(request.getId())
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        CompanyDetailResponse detail = new CompanyDetailResponse();
        detail.setId(item.getId());
        detail.setName(item.getName());
        detail.setTenantCode(item.getTenantCode());
        detail.setStatus(item.getStatus());
        detail.setCreatedAt(item.getCreatedAt());
        detail.setActivatedAt(item.getActivatedAt());
        detail.setRepresentative(item.getRepresentative());
        detail.setBusinessNumber(item.getBusinessNumber());
        detail.setPhone(item.getPhone());
        detail.setEmail(item.getEmail());
        detail.setAddress(item.getAddress());
        detail.setCompanyType(item.getCompanyType());
        detail.setSellerCount(item.getSellerCount());
        detail.setUserCount(item.getUserCount());
        return detail;
    }
}
