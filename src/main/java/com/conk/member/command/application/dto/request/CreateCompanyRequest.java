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
public class CreateCompanyRequest {
    private String id;
    private String tenantCode;

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

    @JsonAlias("adminName")
    private String masterAdminName;

    @JsonAlias("adminEmail")
    private String masterAdminEmail;
}
