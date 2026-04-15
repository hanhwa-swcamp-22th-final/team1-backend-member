package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerRevenueRequest {
    private String tenantId;
    private String sellerId;
    private String month;
    private String keyword;
}
