package com.conk.member.query.service;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.mapper.CompanyQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompanyQueryService {

    private final CompanyQueryMapper companyQueryMapper;

    public CompanyQueryService(CompanyQueryMapper companyQueryMapper) {
        this.companyQueryMapper = companyQueryMapper;
    }

    public List<CompanyListResponse> getCompanies(String keyword, String status) {
        CompanyListRequest request = new CompanyListRequest();
        request.setKeyword(keyword);
        request.setStatus(status);

        List<CompanyListResponse> result = new ArrayList<>();
        for (CompanyListResponse item : companyQueryMapper.findCompanies(request)) {
            result.add(copyCompanyList(item));
        }
        return result;
    }

    public CompanyDetailResponse getCompanyById(String id) {
        CompanyDetailResponse item = companyQueryMapper.findCompanyById(id)
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

    private CompanyListResponse copyCompanyList(CompanyListResponse item) {
        CompanyListResponse dto = new CompanyListResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setTenantCode(item.getTenantCode());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setActivatedAt(item.getActivatedAt());
        dto.setRepresentative(item.getRepresentative());
        dto.setBusinessNumber(item.getBusinessNumber());
        dto.setPhone(item.getPhone());
        dto.setEmail(item.getEmail());
        dto.setAddress(item.getAddress());
        dto.setCompanyType(item.getCompanyType());
        dto.setWarehouseCount(item.getWarehouseCount());
        dto.setSellerCount(item.getSellerCount());
        dto.setUserCount(item.getUserCount());
        return dto;
    }
}
