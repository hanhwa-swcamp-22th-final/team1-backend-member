package com.conk.member.query.dto;

/*
 * MyBatis 사용자 목록 조회 결과를 받는 전용 DTO다.
 * account, role 조인 결과를 화면 응답으로 넘기기 전에 한 번 받아주는 클래스다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserListItem {
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
