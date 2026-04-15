package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSellerRequest {
    private String tenantId;
    private String sellerInfo;
    private String brandNameKo;
    private String brandNameEn;

    @JsonAlias("contactName")
    private String representativeName;

    private String businessNo;
    private String phoneNo;

    @JsonAlias("contactEmail")
    private String email;

    private String categoryName;
    private List<String> warehouseIds;
}
