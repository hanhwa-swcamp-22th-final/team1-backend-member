package com.conk.member.query.dto.condition;

/*
 * 업체 목록 조회 조건을 담는 클래스다.
 * 검색어와 상태값을 받아 업체명/테넌트코드/상태 조건으로 업체를 조회할 때 사용한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanySearchCondition {
    private String keyword;
    private String status;
}
