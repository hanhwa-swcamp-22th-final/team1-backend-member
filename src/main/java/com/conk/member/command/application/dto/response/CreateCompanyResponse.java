package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateCompanyResponse {
    private String id;
    private String tenantCode;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private String masterAdminUserId;
    private String masterAdminEmail;
}
