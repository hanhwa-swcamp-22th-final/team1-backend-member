package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SellerListResponse {
    private String id;
    private String tenantId;
    private String customerCode;
    private String sellerInfo;
    private String brandNameKo;
    private String brandNameEn;
    private String representativeName;
    private String phoneNo;
    private String email;
    private String categoryName;
    private List<String> warehouseIds;
    private String status;
    private LocalDateTime createdAt;
}
