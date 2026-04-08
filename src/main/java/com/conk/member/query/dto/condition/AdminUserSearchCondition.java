package com.conk.member.query.dto.condition;

/*
 * 관리자용 사용자 목록 조회 조건을 담는 클래스다. (MEM-016)
 * companyId(tenantId), role, status, keyword 조건으로 사용자를 조회한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserSearchCondition {
    private String companyId;   // tenant_id 기준
    private String roleName;
    private String accountStatus;
    private String keyword;     // account_name / email / worker_code
}
