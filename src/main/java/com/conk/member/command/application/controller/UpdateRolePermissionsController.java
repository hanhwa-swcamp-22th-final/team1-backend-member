package com.conk.member.command.application.controller;

import com.conk.member.command.application.dto.request.UpdateRolePermissionsRequest;
import com.conk.member.command.application.dto.response.RolePermissionUpdateResponse;
import com.conk.member.command.application.service.UpdateRolePermissionsCommandService;
import com.conk.member.common.security.MemberUserPrincipal;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateRolePermissionsController {

    private final UpdateRolePermissionsCommandService updateRolePermissionsCommandService;

    public UpdateRolePermissionsController(UpdateRolePermissionsCommandService updateRolePermissionsCommandService) {
        this.updateRolePermissionsCommandService = updateRolePermissionsCommandService;
    }

    @PatchMapping("/member/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<RolePermissionUpdateResponse>> updateRolePermissions(
            @PathVariable String roleId,
            @RequestBody UpdateRolePermissionsRequest request,
            @AuthenticationPrincipal MemberUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                "role permissions updated",
                updateRolePermissionsCommandService.updateRolePermissions(
                        roleId,
                        request,
                        principal == null ? null : principal.getAccountId()
                )
        ));
    }
}
