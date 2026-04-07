package com.conk.member.query.dto.mapper;

/*
 * 역할별 권한 매트릭스 조회 시 permission 한 줄(row)을 담는 DTO다.
 * menuName 필드 추가 (MEM-019 응답 명세에 menu_name 포함).
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionMatrixRow {
    private String roleId;
    private String roleName;
    private String permissionId;
    private String menuName;
    private String permissionName;
    private Integer isEnabled;
    private Integer canRead;
    private Integer canWrite;
    private Integer canDelete;
}
