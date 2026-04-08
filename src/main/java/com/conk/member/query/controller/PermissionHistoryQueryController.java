package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.PermissionHistory;
import com.conk.member.query.dto.PermissionHistoryRequest;
import com.conk.member.query.service.PermissionHistoryQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class PermissionHistoryQueryController {

    private final PermissionHistoryQueryService permissionHistoryQueryService;

    public PermissionHistoryQueryController(PermissionHistoryQueryService permissionHistoryQueryService) {
        this.permissionHistoryQueryService = permissionHistoryQueryService;
    }

    @GetMapping("/member/roles/{roleId}/permission-history")
    public ApiResponse<List<PermissionHistory>> getRolePermissionHistory(
            @PathVariable String roleId,
            @RequestParam(required = false) String changedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtTo) {
        PermissionHistoryRequest request = new PermissionHistoryRequest();
        request.setRoleId(roleId);
        request.setChangedBy(changedBy);
        request.setChangedAtFrom(changedAtFrom);
        request.setChangedAtTo(changedAtTo);
        return ApiResponse.ok("role permission history", permissionHistoryQueryService.getRolePermissionHistory(request));
    }
}
