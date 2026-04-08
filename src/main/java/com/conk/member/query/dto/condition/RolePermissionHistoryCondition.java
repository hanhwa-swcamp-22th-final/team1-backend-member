package com.conk.member.query.dto.condition;

/*
 * MEM-021 권한 변경 이력 조회 조건을 담는 클래스다.
 * roleId(필수), changedBy, changedAtFrom, changedAtTo 지원.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionHistoryCondition {
    private String roleId;
    private String changedBy;
    private LocalDateTime changedAtFrom;
    private LocalDateTime changedAtTo;
}
