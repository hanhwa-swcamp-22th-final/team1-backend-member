package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAdminUserResponse {
    private String id;
    private String tenantId;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDateTime updatedAt;
}
