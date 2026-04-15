package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerFeeSummaryResponse {
    private String sellerCode;
    private String sellerName;
    private Double estimatedCost;
    private Double momGrowth;
    private Double turnoverRate;
}
