package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserListResponse {
    private String id;
    private String companyId;
    private String name;
    private String email;
    private String role;
    private String organization;
    private String sellerId;
    private String warehouseId;
    private String workerCode;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
}
