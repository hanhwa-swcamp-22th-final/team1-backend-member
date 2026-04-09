package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionMatrixRequest {
    private String roleId;
    private String roleName;
}
