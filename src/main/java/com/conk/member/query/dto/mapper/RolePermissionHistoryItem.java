package com.conk.member.query.dto.mapper;

/*
 * 권한 변경 이력 MyBatis 조회 결과 DTO.
 * role_permission_history 테이블 구조에 맞게 필드를 정의한다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionHistoryItem {
    private String historyId;
    private String roleId;
    private String roleName;
    private String permissionId;
    private String permissionName;
    private String actionType;
    private String changedBy;
    private LocalDateTime changedAt;
}
