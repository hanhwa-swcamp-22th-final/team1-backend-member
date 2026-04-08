package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.RolePermissionMatrix;
import com.conk.member.query.dto.RolePermissionMatrixRequest;
import com.conk.member.query.service.RolePermissionMatrixQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RolePermissionMatrixQueryController {

    private final RolePermissionMatrixQueryService rolePermissionMatrixQueryService;

    public RolePermissionMatrixQueryController(RolePermissionMatrixQueryService rolePermissionMatrixQueryService) {
        this.rolePermissionMatrixQueryService = rolePermissionMatrixQueryService;
    }

    @GetMapping("/member/roles/permissions")
    public ApiResponse<RolePermissionMatrix> getRolePermissions(RolePermissionMatrixRequest request) {
        return ApiResponse.ok("role permission matrix", rolePermissionMatrixQueryService.getRolePermissions(request));
    }
}
