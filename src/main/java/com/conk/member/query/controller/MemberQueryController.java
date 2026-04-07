package com.conk.member.query.controller;

/*
 * 조회 전용 API를 받는 컨트롤러다.
 * 컨트롤러는 파라미터를 받고, 조회 서비스에 전달하고, 응답만 만든다.
 */

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Profile("legacy-member-query-controller")
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    public MemberQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
    }

    @GetMapping("/member/sellers")
    public ApiResponse<List<QueryResponses.SellerSummary>> getSellerList(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("seller list", memberQueryService.getSellerList(tenantId, status, keyword));
    }

    @GetMapping("/member/users")
    public ApiResponse<List<QueryResponses.UserSummary>> getUsers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(
                "user list",
                memberQueryService.getUsers(tenantId, role, accountStatus, sellerId, warehouseId, keyword)
        );
    }

    @GetMapping("/member/admin/companies")
    public ApiResponse<List<QueryResponses.CompanySummary>> getCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        return ApiResponse.ok("company list", memberQueryService.getCompanies(keyword, status));
    }

    @GetMapping("/member/admin/companies/{id}")
    public ApiResponse<QueryResponses.CompanyDetail> getCompany(@PathVariable String id) {
        return ApiResponse.ok("company detail", memberQueryService.getCompanyById(id));
    }

    @GetMapping("/member/admin/users")
    public ApiResponse<List<QueryResponses.AdminUserSummary>> getAdminUsers(
            @RequestParam(required = false, name = "companyId") String companyId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(
                "admin user list",
                memberQueryService.getAdminUsers(companyId, role, status, keyword)
        );
    }

    @GetMapping("/member/roles/permissions")
    public ApiResponse<QueryResponses.RolePermissionMatrix> getRolePermissions(
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) String roleName) {
        return ApiResponse.ok(
                "role permission matrix",
                memberQueryService.getRolePermissions(roleId, roleName)
        );
    }

    @GetMapping("/member/roles/{roleId}/permission-history")
    public ApiResponse<List<QueryResponses.PermissionHistory>> getRolePermissionHistory(
            @PathVariable String roleId,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtTo) {
        return ApiResponse.ok(
                "role permission history",
                memberQueryService.getRolePermissionHistory(roleId, changedBy, changedAtFrom, changedAtTo)
        );
    }
}
