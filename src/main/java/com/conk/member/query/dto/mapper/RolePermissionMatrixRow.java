package com.conk.member.query.dto.mapper;

/*
 * 역할별 권한 매트릭스 조회 시 permission 한 줄(row)을 담는 DTO다.
 * role_permission과 permission을 조인한 결과를 MyBatis가 바로 매핑하도록 만든 클래스다.
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
    private String permissionName;
    private Integer isEnabled;
    private Integer canRead;
    private Integer canWrite;
    private Integer canDelete;
}
