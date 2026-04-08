package com.conk.member.query.service;

import com.conk.member.query.dto.SellerSummary;
import com.conk.member.query.dto.SellerListRequest;
import com.conk.member.query.dto.SellerListItem;
import com.conk.member.query.mapper.SellerQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SellerQueryService {

    private final SellerQueryMapper sellerQueryMapper;

    public SellerQueryService(SellerQueryMapper sellerQueryMapper) {
        this.sellerQueryMapper = sellerQueryMapper;
    }

    public List<SellerSummary> getSellerList(String tenantId, String status, String keyword) {
        SellerListRequest request = new SellerListRequest();
        request.setTenantId(tenantId);
        request.setStatus(status);
        request.setKeyword(keyword);

        List<SellerSummary> result = new ArrayList<>();
        for (SellerListItem item : sellerQueryMapper.findSellers(request)) {
            result.add(toSellerSummary(item));
        }
        return result;
    }

    private SellerSummary toSellerSummary(SellerListItem item) {
        SellerSummary dto = new SellerSummary();
        dto.setId(item.getId());
        dto.setTenantId(item.getTenantId());
        dto.setCustomerCode(item.getCustomerCode());
        dto.setSellerInfo(item.getSellerInfo());
        dto.setBrandNameKo(item.getBrandNameKo());
        dto.setBrandNameEn(item.getBrandNameEn());
        dto.setRepresentativeName(item.getRepresentativeName());
        dto.setPhoneNo(item.getPhoneNo());
        dto.setEmail(item.getEmail());
        dto.setCategoryName(item.getCategoryName());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }
}
