package com.conk.member.query.dto.condition;

/*
 * 셀러 회사 목록 조회 조건을 담는 클래스다.
 * tenant 기준, 상태 기준, 검색어 기준으로 셀러 회사를 필터링할 때 사용한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerSearchCondition {
    private String tenantId;
    private String status;
    private String keyword;
}
