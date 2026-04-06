package com.conk.member.command.application.dto.request;

/*
 * command 요청 DTO를 한 파일에 모아둔 클래스다.
 * record를 쓰지 않고 class + static inner class 구조로 정리했다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public final class MemberRequests {
    private MemberRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        private String emailOrWorkerCode;
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SetupPasswordRequest {
        private String setupToken;
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class InviteAccountRequest {
        private String role;
        private String tenantId;
        private String sellerId;
        private String warehouseId;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateDirectUserRequest {
        private String tenantId;
        private String warehouseId;
        private String name;
        private String workerCode;
        private String password;
        private String email;
        private String phoneNo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateCompanyRequest {
        private String tenantName;
        private String representativeName;
        private String businessNo;
        private String phoneNo;
        private String email;
        private String address;
        private String tenantType;
        private String masterAdminName;
        private String masterAdminEmail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateAdminUserRequest {
        private String tenantId;
        private String name;
        private String email;
        private String role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateAdminUserRequest {
        private String name;
        private String email;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateSellerRequest {
        private String tenantId;
        private String sellerInfo;
        private String brandNameKo;
        private String brandNameEn;
        private String representativeName;
        private String businessNo;
        private String phoneNo;
        private String email;
        private String categoryName;
        private List<String> warehouseIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRolePermissionsRequest {
        private List<PermissionUpdate> permissions;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class PermissionUpdate {
            private String permissionId;
            private Integer isEnabled;
            private Integer canRead;
            private Integer canWrite;
            private Integer canDelete;
        }
    }
}
