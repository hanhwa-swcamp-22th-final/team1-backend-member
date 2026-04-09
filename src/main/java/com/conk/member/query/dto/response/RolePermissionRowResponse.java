package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionRowResponse {
    private String permissionId;
    private String menuName;
    private String permissionName;
    private Integer isEnabled;
    private Integer canRead;
    private Integer canWrite;
    private Integer canDelete;
    private LocalDateTime changedAt;
}
