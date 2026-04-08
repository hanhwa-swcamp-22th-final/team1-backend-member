package com.conk.member.query.dto;

/*
 * MyBatis 셀러 회사 목록 조회 결과를 담는 DTO다.
 * seller 테이블 기준 조회 결과를 서비스 계층에서 응답 DTO로 변환하기 전에 사용한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SellerListItem {
    private String id;
    private String tenantId;
    private String customerCode;
    private String sellerInfo;
    private String brandNameKo;
    private String brandNameEn;
    private String representativeName;
    private String phoneNo;
    private String email;
    private String categoryName;
    private String status;
    private LocalDateTime createdAt;
}
