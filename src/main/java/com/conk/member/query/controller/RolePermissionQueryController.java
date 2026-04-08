package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class RolePermissionQueryController {

    private final MemberQueryService memberQueryService;

    public RolePermissionQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
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
