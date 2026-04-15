package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.query.dto.request.SellerFeeSummaryRequest;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.request.SellerRevenueRequest;
import com.conk.member.query.dto.request.SellerStatsRequest;
import com.conk.member.query.dto.response.SellerFeeSummaryResponse;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerRevenueResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.mapper.SellerMetricQueryMapper;
import com.conk.member.query.mapper.SellerQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SellerQueryService {

    private final SellerQueryMapper sellerQueryMapper;
    private final SellerMetricQueryMapper sellerMetricQueryMapper;
    private final SellerWarehouseRepository sellerWarehouseRepository;

    public SellerQueryService(SellerQueryMapper sellerQueryMapper,
                              SellerMetricQueryMapper sellerMetricQueryMapper,
                              SellerWarehouseRepository sellerWarehouseRepository) {
        this.sellerQueryMapper = sellerQueryMapper;
        this.sellerMetricQueryMapper = sellerMetricQueryMapper;
        this.sellerWarehouseRepository = sellerWarehouseRepository;
    }

    public List<SellerListResponse> getSellerList(SellerListRequest request) {
        List<SellerListResponse> result = new ArrayList<>();
        for (SellerListResponse item : sellerQueryMapper.findSellers(request)) {
            result.add(toSellerListResponse(item));
        }
        return result;
    }

    public List<SellerFeeSummaryResponse> getSellerFeeSummary(SellerFeeSummaryRequest request) {
        List<SellerFeeSummaryResponse> result = new ArrayList<>();
        for (SellerFeeSummaryResponse item : sellerMetricQueryMapper.findSellerFeeSummary(request)) {
            SellerFeeSummaryResponse dto = new SellerFeeSummaryResponse();
            dto.setSellerCode(item.getSellerCode());
            dto.setSellerName(item.getSellerName());
            dto.setEstimatedCost(item.getEstimatedCost());
            dto.setMomGrowth(item.getMomGrowth());
            dto.setTurnoverRate(item.getTurnoverRate());
            result.add(dto);
        }
        return result;
    }

    public List<SellerRevenueResponse> getSellerRevenue(SellerRevenueRequest request) {
        List<SellerRevenueResponse> result = new ArrayList<>();
        for (SellerRevenueResponse item : sellerMetricQueryMapper.findSellerRevenue(request)) {
            SellerRevenueResponse dto = new SellerRevenueResponse();
            dto.setSellerCode(item.getSellerCode());
            dto.setSellerName(item.getSellerName());
            dto.setMonthRevenue(item.getMonthRevenue());
            dto.setTotalOrders(item.getTotalOrders());
            dto.setAvgOrderValue(item.getAvgOrderValue());
            result.add(dto);
        }
        return result;
    }

    public SellerStatsResponse getSellerStats(SellerStatsRequest request) {
        SellerStatsResponse item = sellerMetricQueryMapper.findSellerStats(request);
        SellerStatsResponse response = new SellerStatsResponse();
        if (item == null) {
            response.setActiveSellerCount(0);
            response.setInactiveSellerCount(0);
            response.setTotalSellerCount(0);
            return response;
        }

        response.setActiveSellerCount(item.getActiveSellerCount() == null ? 0 : item.getActiveSellerCount());
        response.setInactiveSellerCount(item.getInactiveSellerCount() == null ? 0 : item.getInactiveSellerCount());
        response.setTotalSellerCount(item.getTotalSellerCount() == null ? 0 : item.getTotalSellerCount());
        return response;
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
