package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateDirectUserResponse {
    private String id;
    private String role;
    private String name;
    private String workerCode;
    private String tenantId;
    private String warehouseId;
    private String accountStatus;
}
