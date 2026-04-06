package com.conk.member.query.controller;

/*
 * query API를 한곳에 모은 컨트롤러다.
 * 목록성 조회는 MyBatis 기반 query service를 사용하고, 응답 래퍼 형식도 API 명세서에 맞춰 맞춘다.
 */

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/member/sellers")
    public ApiResponse<List<QueryResponses.SellerSummary>> getSellerList(@RequestParam(required = false) String tenantId,
                                                                         @RequestParam(required = false) String status,
                                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("seller list", memberQueryService.getSellerList(tenantId, status, keyword));
    }

    @GetMapping("/member/users")
    public ApiResponse<List<QueryResponses.UserSummary>> getUsers(@RequestParam(required = false) String tenantId,
                                                                  @RequestParam(required = false) String role,
                                                                  @RequestParam(required = false) String accountStatus,
                                                                  @RequestParam(required = false) String sellerId,
                                                                  @RequestParam(required = false) String warehouseId,
                                                                  @RequestParam(required = false) String keyword) {
        return ApiResponse.ok("user list", memberQueryService.getUsers(tenantId, role, accountStatus, sellerId, warehouseId, keyword));
    }

    @GetMapping("/member/admin/users")
    public Map<String, Object> getAdminUsers(@RequestParam(required = false, name = "companyId") String companyId,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false, name = "status") String status,
                                             @RequestParam(required = false) String keyword) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", memberQueryService.getUsers(companyId, role, status, null, null, keyword));
        return payload;
    }

    @GetMapping("/member/admin/companies")
    public Map<String, Object> getCompanies(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", memberQueryService.getCompanies(keyword, status));
        return payload;
    }

    @GetMapping("/member/roles/permissions")
    public ApiResponse<QueryResponses.RolePermissionMatrix> getRolePermissions(@RequestParam String roleId) {
        return ApiResponse.ok("role permission matrix", memberQueryService.getRolePermissions(roleId));
    }

    @GetMapping("/member/roles/{roleId}/permission-history")
    public ApiResponse<List<QueryResponses.PermissionHistory>> getRolePermissionHistory(@PathVariable String roleId) {
        return ApiResponse.ok("role permission history", memberQueryService.getRolePermissionHistory(roleId));
    }
}
