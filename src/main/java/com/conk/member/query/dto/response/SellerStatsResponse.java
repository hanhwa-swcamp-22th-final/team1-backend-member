package com.conk.member.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerStatsResponse {

    private final int activeSellerCount;
    private final int newThisMonth;
    private final String trendType;
}
