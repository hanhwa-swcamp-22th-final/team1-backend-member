package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.mapper.SellerQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        SellerListRequest normalizedRequest = normalizeRequest(request);
        List<SellerListResponse> sellers = sellerQueryMapper.findSellers(normalizedRequest);
        if (sellers.isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> warehouseIdsBySeller = loadWarehouseIdsBySeller(sellers);
        List<SellerListResponse> result = new ArrayList<>();
        for (SellerListResponse item : sellers) {
            result.add(toSellerListResponse(item, warehouseIdsBySeller));
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

    private SellerListResponse toSellerListResponse(SellerListResponse item, Map<String, List<String>> warehouseIdsBySeller) {
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
        dto.setWarehouseIds(warehouseIdsBySeller.getOrDefault(item.getId(), List.of()));
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    private Map<String, List<String>> loadWarehouseIdsBySeller(List<SellerListResponse> sellers) {
        List<String> sellerIds = sellers.stream()
                .map(SellerListResponse::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        Map<String, List<String>> warehouseIdsBySeller = new LinkedHashMap<>();
        for (SellerWarehouse mapping : sellerWarehouseRepository.findBySellerIdInOrderBySellerIdAscWarehouseIdAsc(sellerIds)) {
            warehouseIdsBySeller
                    .computeIfAbsent(mapping.getSellerId(), ignored -> new ArrayList<>())
                    .add(mapping.getWarehouseId());
        }
        return warehouseIdsBySeller;
    }

    private SellerListRequest normalizeRequest(SellerListRequest request) {
        SellerListRequest normalized = new SellerListRequest();
        normalized.setTenantId(normalizeText(request.getTenantId()));
        normalized.setStatus(normalizeText(request.getStatus()));
        normalized.setKeyword(normalizeText(request.getKeyword()));
        return normalized;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
