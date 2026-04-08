package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionMatrix {
    private String roleId;
    private String roleName;
    private List<RolePermissionRow> permissions;
}
