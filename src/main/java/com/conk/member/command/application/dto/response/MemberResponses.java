package com.conk.member.command.application.dto.response;

/*
 * command 응답 DTO를 한 파일에 모아둔 클래스다.
 * 클래스 수를 줄이면서도 class 기반 구조를 유지하기 위해 static inner class를 사용했다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


public final class MemberResponses {
    private MemberResponses() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginResponse {
        private String token;           // accessToken
        private String refreshToken;    // refreshToken (추가)
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

    @Getter @Setter @NoArgsConstructor
    public static class SetupPasswordResponse {
        private String accountId;
        private String accountStatus;
        private LocalDateTime passwordChangedAt;
        private String tenantStatus;
        private LocalDateTime activatedAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class InviteAccountResponse {
        private String invitationId;
        private String role;
        private String tenantId;
        private String sellerId;
        private String warehouseId;
        private String name;
        private String email;
        private String inviteStatus;
        private LocalDateTime inviteSentAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SimpleUserStatusResponse {
        private String accountStatus;
        private Boolean isTemporaryPassword;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateDirectUserResponse {
        private String id;
        private String role;
        private String name;
        private String workerCode;
        private String tenantId;
        private String warehouseId;
        private String accountStatus;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateCompanyResponse {
        private String id;
        private String tenantCode;
        private String name;
        private String status;
        private LocalDateTime createdAt;
        private String masterAdminUserId;
        private String masterAdminEmail;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateAdminUserResponse {
        private String id;
        private String tenantId;
        private String name;
        private String email;
        private String role;
        private String status;
        private String invitationId;
    }

    @Getter @Setter @NoArgsConstructor
    public static class UpdateAdminUserResponse {
        private String id;
        private String tenantId;
        private String name;
        private String email;
        private String role;
        private String status;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class CreateSellerResponse {
        private String id;
        private String customerCode;
        private String brandNameKo;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class RolePermissionUpdateResponse {
        private String roleId;
        private int updatedPermissionCount;   // MEM-020: updatedPermissionCount
        private LocalDateTime changedAt;      // MEM-020
        private String changedBy;             // MEM-020
    }

    @Getter @Setter @NoArgsConstructor
    public static class UpdateCompanyResponse {
        private String id;
        private String tenantCode;
        private String name;
        private String status;
        private java.time.LocalDateTime updatedAt;
    }
}
