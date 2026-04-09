package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerListRequest {
    private String tenantId;
    private String status;
    private String keyword;
}
