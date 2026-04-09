package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionMatrixResponse {
    private String roleId;
    private String roleName;
    private List<RolePermissionRowResponse> permissions;
}
