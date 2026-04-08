package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String id;
    private String email;
    private String name;
    private String role;
    private String status;
    private String tenantId;
    private String tenantName;
    private String sellerId;
    private String warehouseId;
}
