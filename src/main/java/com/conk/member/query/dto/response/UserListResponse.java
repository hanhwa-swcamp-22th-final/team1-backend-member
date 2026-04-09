package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserListResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String accountStatus;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String workerCode;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
