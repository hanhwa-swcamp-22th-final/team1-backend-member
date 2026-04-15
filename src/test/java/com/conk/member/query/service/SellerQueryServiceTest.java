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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SellerQueryServiceTest {

    @Mock SellerQueryMapper sellerQueryMapper;
    @Mock SellerMetricQueryMapper sellerMetricQueryMapper;
    @Mock SellerWarehouseRepository sellerWarehouseRepository;

    @InjectMocks SellerQueryService sellerQueryService;

    @Test
    @DisplayName("셀러 목록 조회 성공 - 창고 ID 포함")
    void getSellerList_success() {
        SellerListResponse item = new SellerListResponse();
        item.setId("SELLER-001");
        item.setTenantId("TENANT-001");
        item.setBrandNameKo("테스트브랜드");
        item.setStatus("ACTIVE");

        SellerWarehouse sw = new SellerWarehouse();
        sw.setWarehouseId("WH-001");

        given(sellerQueryMapper.findSellers(any())).willReturn(List.of(item));
        given(sellerWarehouseRepository.findBySellerIdOrderByWarehouseIdAsc("SELLER-001"))
                .willReturn(List.of(sw));

        List<SellerListResponse> result = sellerQueryService.getSellerList(new SellerListRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("SELLER-001");
        assertThat(result.get(0).getBrandNameKo()).isEqualTo("테스트브랜드");
        assertThat(result.get(0).getWarehouseIds()).containsExactly("WH-001");
    }

    @Test
    @DisplayName("필터 조건으로 셀러 목록 조회")
    void getSellerList_withFilter() {
        SellerListRequest request = new SellerListRequest();
        request.setTenantId("TENANT-001");
        request.setStatus("ACTIVE");

        given(sellerQueryMapper.findSellers(request)).willReturn(List.of());

        List<SellerListResponse> result = sellerQueryService.getSellerList(request);

        assertThat(result).isEmpty();
        then(sellerQueryMapper).should().findSellers(request);
    }

    @Test
    @DisplayName("셀러별 3PL 비용 요약 조회")
    void getSellerFeeSummary_success() {
        SellerFeeSummaryResponse item = new SellerFeeSummaryResponse();
        item.setSellerCode("SELLER-001");
        item.setSellerName("테스트브랜드");
        item.setEstimatedCost(0.0);

        given(sellerMetricQueryMapper.findSellerFeeSummary(any())).willReturn(List.of(item));

        List<SellerFeeSummaryResponse> result = sellerQueryService.getSellerFeeSummary(new SellerFeeSummaryRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSellerCode()).isEqualTo("SELLER-001");
    }

    @Test
    @DisplayName("셀러별 당월 매출 조회")
    void getSellerRevenue_success() {
        SellerRevenueResponse item = new SellerRevenueResponse();
        item.setSellerCode("SELLER-001");
        item.setSellerName("테스트브랜드");
        item.setMonthRevenue(0.0);

        given(sellerMetricQueryMapper.findSellerRevenue(any())).willReturn(List.of(item));

        List<SellerRevenueResponse> result = sellerQueryService.getSellerRevenue(new SellerRevenueRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSellerName()).isEqualTo("테스트브랜드");
    }

    @Test
    @DisplayName("활성 셀러 수 통계 조회")
    void getSellerStats_success() {
        SellerStatsResponse item = new SellerStatsResponse();
        item.setActiveSellerCount(2);
        item.setInactiveSellerCount(1);
        item.setTotalSellerCount(3);

        given(sellerMetricQueryMapper.findSellerStats(any())).willReturn(item);

        SellerStatsResponse result = sellerQueryService.getSellerStats(new SellerStatsRequest());

        assertThat(result.getActiveSellerCount()).isEqualTo(2);
        assertThat(result.getInactiveSellerCount()).isEqualTo(1);
        assertThat(result.getTotalSellerCount()).isEqualTo(3);
    }
}
