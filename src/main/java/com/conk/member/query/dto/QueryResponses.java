package com.conk.member.query.dto;

/*
 * query 응답 DTO를 한 파일에 모아둔 클래스다.
 * MEM-003/006/012/013/019/021 명세 기준으로 필드를 정의했다.
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public final class QueryResponses {
    private QueryResponses() {
    }

    /* MEM-003: 셀러 목록 조회 */
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

    /* MEM-006: 소속 사용자 목록 조회 */
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

    /* MEM-012: 업체 목록 조회 (admin raw payload) */
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

    /* MEM-013: 업체 단건 조회 (admin raw payload) */
    @Getter @Setter @NoArgsConstructor
    public static class CompanyDetail {
        private String id;
        private String name;
        private String tenantCode;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime activatedAt;
        private String representative;
        private String businessNumber;
        private String phone;
        private String email;
        private String address;
        private String companyType;
        private Integer sellerCount;
        private Integer userCount;
    }

    /* MEM-016: 관리자용 사용자 목록 (admin raw payload) */
    @Getter @Setter @NoArgsConstructor
    public static class AdminUserSummary {
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

    /* MEM-019: 역할별 권한 매트릭스 조회 */
    @Getter @Setter @NoArgsConstructor
    public static class RolePermissionMatrix {
        private String roleId;
        private String roleName;
        private List<PermissionRow> permissions;

        @Getter @Setter @NoArgsConstructor
        public static class PermissionRow {
            private String permissionId;
            private String menuName;
            private String permissionName;
            private Integer isEnabled;
            private Integer canRead;
            private Integer canWrite;
            private Integer canDelete;
            private LocalDateTime changedAt;
        }
    }

    /* MEM-021: 권한 변경 이력 조회 */
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
}
