package com.conk.member.query.service;

import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.query.dto.CompanySummary;
import com.conk.member.query.dto.CompanyListRequest;
import com.conk.member.query.dto.CompanyListItem;
import com.conk.member.query.mapper.CompanyQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompanyListQueryService {

    private final CompanyQueryMapper companyQueryMapper;
    private final AccountRepository accountRepository;
    private final SellerWarehouseRepository sellerWarehouseRepository;

    public CompanyListQueryService(CompanyQueryMapper companyQueryMapper,
                                   AccountRepository accountRepository,
                                   SellerWarehouseRepository sellerWarehouseRepository) {
        this.companyQueryMapper = companyQueryMapper;
        this.accountRepository = accountRepository;
        this.sellerWarehouseRepository = sellerWarehouseRepository;
    }

    public List<CompanySummary> getCompanies(CompanyListRequest request) {
        List<CompanySummary> result = new ArrayList<>();
        for (CompanyListItem item : companyQueryMapper.findCompanies(request)) {
            result.add(toCompanySummary(item));
        }
        return result;
    }

    private CompanySummary toCompanySummary(CompanyListItem item) {
        CompanySummary dto = new CompanySummary();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setTenantCode(item.getTenantCode());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setRepresentative(item.getRepresentative());
        dto.setBusinessNumber(item.getBusinessNumber());
        dto.setPhone(item.getPhone());
        dto.setEmail(item.getEmail());
        dto.setAddress(item.getAddress());
        dto.setCompanyType(item.getCompanyType());
        dto.setWarehouseCount(countWarehousesForTenant(item.getId()));
        dto.setSellerCount(item.getSellerCount());
        dto.setUserCount(item.getUserCount());
        return dto;
    }

    private int countWarehousesForTenant(String tenantId) {
        LinkedHashSet<String> warehouseIds = new LinkedHashSet<>();
        warehouseIds.addAll(accountRepository.findDistinctWarehouseIdsByTenantId(tenantId));
        warehouseIds.addAll(sellerWarehouseRepository.findDistinctWarehouseIdsByTenantId(tenantId));
        return warehouseIds.size();
    }
}
