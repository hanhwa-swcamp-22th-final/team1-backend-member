package com.conk.member.query.dto.condition;

/*
 * 업체 목록 조회 조건을 담는 클래스다.
 * MEM-012: keyword, status, sortBy, sortOrder 지원.
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
    private String sortBy;    // tenant_name / status / created_at
    private String sortOrder; // asc / desc
}
