package com.conk.member.query.dto;

/*
 * MyBatis 업체 목록/단건 조회 결과를 담는 DTO다.
 * activatedAt 추가 (MEM-013 단건 조회 응답용).
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
