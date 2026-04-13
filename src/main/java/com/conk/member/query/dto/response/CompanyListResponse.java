package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyListResponse {
    private String id;
    private String name;
    private String tenantCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private String representative;
    private String businessNumber;
    private String phone;
    private String email;
    private String address;
    private String companyType;
    private Integer warehouseCount;
    private Integer sellerCount;
    private Integer userCount;
}
