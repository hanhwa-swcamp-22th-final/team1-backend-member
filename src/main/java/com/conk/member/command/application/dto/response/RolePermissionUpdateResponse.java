package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionUpdateResponse {
    private String roleId;
    private int updatedPermissionCount;
    private LocalDateTime changedAt;
    private String changedBy;
}
