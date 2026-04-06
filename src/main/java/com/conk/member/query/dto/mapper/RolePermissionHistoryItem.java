package com.conk.member.query.dto.mapper;

/*
 * 권한 변경 이력 조회 결과를 담는 DTO다.
 * 변경 시각, 변경자, 대상 권한, 변경 유형을 화면에 내려주기 위한 전용 조회 클래스다.
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
