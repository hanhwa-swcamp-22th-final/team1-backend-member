package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InviteAccountRequest {
    private String role;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String name;
    private String email;
}
