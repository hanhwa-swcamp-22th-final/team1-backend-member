package com.conk.member.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CompanyDetailResponse {
    private String id;
    private String name;
    private String tenantCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private String representative;
    private String businessNumber;
    private String phone;
    private String email;
    private String address;
    private String companyType;
    private Integer sellerCount;
    private Integer userCount;

    /** wms-service에서 조회한 소속 창고 목록 */
    private List<WarehouseItem> warehouseList;

    /** member DB의 Seller.brandNameKo 기반 등록 셀러 회사명 목록 */
    private List<String> sellerCompanyList;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WarehouseItem {
        private String code;
        private String name;
        private String status;
    }
}
