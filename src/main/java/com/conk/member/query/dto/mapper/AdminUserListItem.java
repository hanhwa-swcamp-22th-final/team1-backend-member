package com.conk.member.query.dto.mapper;

/*
 * MyBatis 관리자용 사용자 목록 조회 결과를 담는 DTO다. (MEM-016)
 * account + role + tenant 조인 결과를 받는다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserListItem {
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
