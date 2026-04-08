package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionRow {
    private String permissionId;
    private String menuName;
    private String permissionName;
    private Integer isEnabled;
    private Integer canRead;
    private Integer canWrite;
    private Integer canDelete;
    private LocalDateTime changedAt;
}
