package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermissionUpdateRequest {
    private String permissionId;
    private Integer isEnabled;
    private Integer canRead;
    private Integer canWrite;
    private Integer canDelete;
}
