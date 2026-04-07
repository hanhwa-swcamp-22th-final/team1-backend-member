package com.conk.member.query.dto.mapper;

/*
 * MyBatis 업체 단건 조회 결과를 담는 DTO다. (MEM-013)
 * tenant 기본 정보 + seller/account 집계값을 포함한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyDetailItem {
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
    private Integer sellerCount;
    private Integer userCount;
}
