package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCompanyRequest {
    @JsonAlias("name")
    private String tenantName;

    @JsonAlias("representative")
    private String representativeName;

    @JsonAlias("businessNumber")
    private String businessNo;

    @JsonAlias("phone")
    private String phoneNo;

    private String email;
    private String address;

    @JsonAlias("companyType")
    private String tenantType;

    private String status;
}
