package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateDirectUserRequest {
    private String tenantId;
    private String warehouseId;
    private String name;
    private String workerCode;
    private String password;
    private String email;
    private String phoneNo;
}
