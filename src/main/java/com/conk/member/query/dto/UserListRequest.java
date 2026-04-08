package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserListRequest {
    private String tenantId;
    private String role;
    private String accountStatus;
    private String sellerId;
    private String warehouseId;
    private String keyword;
}
