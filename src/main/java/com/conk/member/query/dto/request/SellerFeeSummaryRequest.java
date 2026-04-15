package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerFeeSummaryRequest {
    private String tenantId;
    private String sellerId;
    private String keyword;
}
