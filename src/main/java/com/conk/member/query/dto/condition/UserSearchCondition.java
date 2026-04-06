package com.conk.member.query.dto.condition;

/*
 * 관리자용 사용자 목록 조회와 소속 사용자 목록 조회에서 공통으로 사용하는 검색 조건 클래스다.
 * tenant/role/status/seller/warehouse/keyword 값을 담아서 MyBatis 동적 SQL 파라미터로 전달한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSearchCondition {
    private String tenantId;
    private String roleName;
    private String accountStatus;
    private String sellerId;
    private String warehouseId;
    private String keyword;
}
