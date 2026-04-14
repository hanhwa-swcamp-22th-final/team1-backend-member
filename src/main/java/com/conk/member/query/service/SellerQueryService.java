package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.mapper.SellerQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SellerQueryService {

    private final SellerQueryMapper sellerQueryMapper;
    private final SellerWarehouseRepository sellerWarehouseRepository;

    public SellerQueryService(SellerQueryMapper sellerQueryMapper,
                              SellerWarehouseRepository sellerWarehouseRepository) {
        this.sellerQueryMapper = sellerQueryMapper;
        this.sellerWarehouseRepository = sellerWarehouseRepository;
    }

    public List<SellerListResponse> getSellerList(SellerListRequest request) {
        List<SellerListResponse> result = new ArrayList<>();
        for (SellerListResponse item : sellerQueryMapper.findSellers(request)) {
            result.add(toSellerListResponse(item));
        }
        return result;
    }

    public SellerStatsResponse getSellerStats() {
        List<SellerListResponse> sellers = getSellerList(new SellerListRequest());
        YearMonth currentMonth = YearMonth.now();

        int activeSellerCount = (int) sellers.stream()
                .filter(seller -> "ACTIVE".equalsIgnoreCase(seller.getStatus()))
                .count();

        int newThisMonth = (int) sellers.stream()
                .filter(seller -> seller.getCreatedAt() != null)
                .map(seller -> seller.getCreatedAt().toLocalDate())
                .filter(createdDate -> YearMonth.from(createdDate).equals(currentMonth))
                .count();

        String trendType = newThisMonth > 0 ? "up" : "neutral";

        return SellerStatsResponse.builder()
                .activeSellerCount(activeSellerCount)
                .newThisMonth(newThisMonth)
                .trendType(trendType)
                .build();
    }

    private SellerListResponse toSellerListResponse(SellerListResponse item) {
        SellerListResponse dto = new SellerListResponse();
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
