package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RolePermissionHistoryItem {
    private String historyId;
    private String rolePermissionId;
    private Integer beforeIsEnabled;
    private Integer beforeCanRead;
    private Integer beforeCanWrite;
    private Integer beforeCanDelete;
    private Integer afterCanRead;
    private Integer afterCanWrite;
    private Integer afterCanDelete;
    private String changedBy;
    private LocalDateTime changedAt;
}
