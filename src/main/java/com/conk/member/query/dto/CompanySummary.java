package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanySummary {
    private String id;
    private String name;
    private String tenantCode;
    private String status;
    private LocalDateTime createdAt;
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
