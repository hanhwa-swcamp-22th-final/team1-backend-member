package com.conk.member.query.dto.mapper;

/*
 * MyBatis 업체 목록 조회 결과를 담는 DTO다.
 * tenant 기반 기본 정보 + seller/account 집계값을 함께 실어 나를 때 사용한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyListItem {
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
