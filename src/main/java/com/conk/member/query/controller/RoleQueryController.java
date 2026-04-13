package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.PermissionHistoryRequest;
import com.conk.member.query.dto.request.RolePermissionMatrixRequest;
import com.conk.member.query.dto.response.PermissionHistoryResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixResponse;
import com.conk.member.query.service.RoleQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/member/roles")
public class RoleQueryController {

    private final RoleQueryService roleQueryService;

    public RoleQueryController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    @GetMapping("/permissions")
    public ApiResponse<RolePermissionMatrixResponse> getRolePermissions(RolePermissionMatrixRequest request) {
        return ApiResponse.ok("role permission matrix", roleQueryService.getRolePermissions(request));
    }

    @GetMapping("/{roleId}/permission-history")
    public ApiResponse<List<PermissionHistoryResponse>> getRolePermissionHistory(
            @PathVariable String roleId,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtTo) {
        PermissionHistoryRequest request = new PermissionHistoryRequest();
        request.setRoleId(roleId);
        request.setChangedBy(changedBy);
        request.setChangedAtFrom(changedAtFrom);
        request.setChangedAtTo(changedAtTo);
        return ApiResponse.ok("role permission history", roleQueryService.getRolePermissionHistory(request));
    }
}
