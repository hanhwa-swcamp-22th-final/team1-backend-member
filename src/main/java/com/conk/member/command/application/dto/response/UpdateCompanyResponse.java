package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCompanyResponse {
    private String id;
    private String tenantCode;
    private String name;
    private String status;
    private LocalDateTime updatedAt;
}
