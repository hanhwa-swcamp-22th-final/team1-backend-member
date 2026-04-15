package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerRevenueResponse {
    private String sellerCode;
    private String sellerName;
    private Double monthRevenue;
    private Integer totalOrders;
    private Double avgOrderValue;
}
