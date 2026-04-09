package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.mapper.SellerQueryMapper;
import com.conk.member.query.service.SellerQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SellerQueryServiceTest {

    @Mock SellerQueryMapper sellerQueryMapper;
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
    @DisplayName("셀러 목록 - 창고 없는 셀러")
    void getSellerList_noWarehouse() {
        SellerListResponse item = new SellerListResponse();
        item.setId("SELLER-001");
        item.setStatus("ACTIVE");

        given(sellerQueryMapper.findSellers(any())).willReturn(List.of(item));
        given(sellerWarehouseRepository.findBySellerIdOrderByWarehouseIdAsc("SELLER-001"))
                .willReturn(List.of());

        List<SellerListResponse> result = sellerQueryService.getSellerList(new SellerListRequest());

        assertThat(result.get(0).getWarehouseIds()).isEmpty();
    }

    @Test
    @DisplayName("셀러 목록 - 결과 없음")
    void getSellerList_empty() {
        given(sellerQueryMapper.findSellers(any())).willReturn(List.of());

        List<SellerListResponse> result = sellerQueryService.getSellerList(new SellerListRequest());

        assertThat(result).isEmpty();
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
}
