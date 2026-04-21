package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateSellerRequest {
    private String tenantId;
    private String sellerInfo="test";
    private String brandNameKo;
    private String brandNameEn;
    private String contactName;
    private String businessNo="012-1231-123";
    private String phoneNo="010-1234-1234";
    private String contactEmail;
    private String categoryName="test";
    private List<String> warehouseIds;
}
