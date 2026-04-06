package com.conk.member.query.dto;

/*
 * query 응답 DTO를 한 파일에 모아둔 클래스다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public final class QueryResponses {
    private QueryResponses() {
    }

    @Getter @Setter @NoArgsConstructor
    public static class SellerSummary {
        private String id;
        private String tenantId;
        private String customerCode;
        private String sellerInfo;
        private String brandNameKo;
        private String brandNameEn;
        private String representativeName;
        private String phoneNo;
        private String email;
        private String categoryName;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class UserSummary {
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

    @Getter @Setter @NoArgsConstructor
    public static class CompanySummary {
        private String id;
        private String name;
        private String tenantCode;
        private String status;
        private LocalDateTime createdAt;
        private String representative;
        private String businessNumber;
        private String phone;
        private String email;
        private String address;
        private String companyType;
        private Integer warehouseCount;
        private Integer sellerCount;
        private Integer userCount;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PermissionHistory {
        private String historyId;
        private String roleId;
        private String roleName;
        private String permissionId;
        private String permissionName;
        private String actionType;
        private String changedBy;
        private LocalDateTime changedAt;
    }

    @Getter @Setter @NoArgsConstructor
    public static class RolePermissionMatrix {
        private String roleId;
        private String roleName;
        private List<PermissionRow> permissions;

        @Getter @Setter @NoArgsConstructor
        public static class PermissionRow {
            private String permissionId;
            private Integer isEnabled;
            private Integer canRead;
            private Integer canWrite;
            private Integer canDelete;
        }
    }
}
