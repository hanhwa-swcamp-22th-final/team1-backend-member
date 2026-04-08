package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
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
public class SellerListQueryService {

    private final SellerQueryMapper sellerQueryMapper;
    private final SellerWarehouseRepository sellerWarehouseRepository;

    public SellerListQueryService(SellerQueryMapper sellerQueryMapper,
                                  SellerWarehouseRepository sellerWarehouseRepository) {
        this.sellerQueryMapper = sellerQueryMapper;
        this.sellerWarehouseRepository = sellerWarehouseRepository;
    }

    public List<SellerSummary> getSellerList(SellerListRequest request) {
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
        dto.setWarehouseIds(getWarehouseIdsForSeller(item.getId()));
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    private List<String> getWarehouseIdsForSeller(String sellerId) {
        List<String> warehouseIds = new ArrayList<>();
        for (SellerWarehouse mapping : sellerWarehouseRepository.findBySellerIdOrderByWarehouseIdAsc(sellerId)) {
            warehouseIds.add(mapping.getWarehouseId());
        }
        return warehouseIds;
    }
}
