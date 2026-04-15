package com.conk.member.query.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerStatsResponse {
    private Integer activeSellerCount;
    private Integer inactiveSellerCount;
    private Integer totalSellerCount;

    @JsonProperty("newThisMonth")
    public Integer getNewThisMonth() {
        return 0;
    }

    @JsonProperty("trendType")
    public String getTrendType() {
        return "neutral";
    }
}
