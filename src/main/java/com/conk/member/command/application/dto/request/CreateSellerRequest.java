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
    private String sellerInfo;
    private String brandNameKo;
    private String brandNameEn;
    private String representativeName;
    private String businessNo;
    private String phoneNo;
    private String email;
    private String categoryName;
    private List<String> warehouseIds;
}
