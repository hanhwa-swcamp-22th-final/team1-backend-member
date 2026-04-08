package com.conk.member.query.dto.condition;

/*
 * 권한 변경 이력 조회 조건 클래스다. (MEM-021)
 * roleId(필수), changedBy, changedAtFrom, changedAtTo 조건을 담는다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PermissionHistorySearchCondition {
    private String roleId;
    private String changedBy;
    private LocalDateTime changedAtFrom;
    private LocalDateTime changedAtTo;
}
