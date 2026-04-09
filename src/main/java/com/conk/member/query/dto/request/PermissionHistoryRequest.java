package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PermissionHistoryRequest {
    private String roleId;
    private String changedBy;
    private LocalDateTime changedAtFrom;
    private LocalDateTime changedAtTo;
}
