package com.conk.member.command.controller;

/*
 * command API를 한곳에 모은 컨트롤러다.
 * 일반 API는 ApiResponse를 사용하고, admin raw payload 요구사항이 있는 API는 DTO를 직접 반환한다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.application.service.MemberCommandService;
import com.conk.member.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberCommandController {

    private final MemberCommandService memberCommandService;

    @PostMapping("/member/auth/login")
    public ApiResponse<MemberResponses.LoginResponse> login(@RequestBody MemberRequests.LoginRequest request) {
        return ApiResponse.ok("login", memberCommandService.login(request));
    }

    @PostMapping("/member/auth/setup-password")
    public ApiResponse<MemberResponses.SetupPasswordResponse> setupPassword(@RequestBody MemberRequests.SetupPasswordRequest request) {
        return ApiResponse.ok("setup password", memberCommandService.setupPassword(request));
    }

    @PostMapping("/member/auth/invite")
    public ApiResponse<MemberResponses.InviteAccountResponse> invite(@RequestBody MemberRequests.InviteAccountRequest request,
                                                                     @RequestHeader(value = "X-Invoker-Account-Id", required = false) String invokerId) {
        return ApiResponse.ok("invite sent", memberCommandService.invite(request, invokerId));
    }

    @PostMapping("/member/users/{userId}/reset-password")
    public ApiResponse<MemberResponses.SimpleUserStatusResponse> resetPassword(@PathVariable String userId) {
        return ApiResponse.ok("password reset", memberCommandService.resetPassword(userId));
    }

    @PostMapping("/member/users/{userId}/deactivate")
    public ApiResponse<MemberResponses.SimpleUserStatusResponse> deactivate(@PathVariable String userId) {
        return ApiResponse.ok("user deactivated", memberCommandService.deactivate(userId));
    }

    @PostMapping("/member/users/{userId}/reactivate")
    public ApiResponse<MemberResponses.SimpleUserStatusResponse> reactivate(@PathVariable String userId) {
        return ApiResponse.ok("user reactivated", memberCommandService.reactivate(userId));
    }

    @PostMapping("/member/users/direct")
    public ApiResponse<MemberResponses.CreateDirectUserResponse> createDirect(@RequestBody MemberRequests.CreateDirectUserRequest request) {
        return ApiResponse.ok("worker created", memberCommandService.createDirect(request));
    }

    @PostMapping("/member/sellers")
    public ApiResponse<MemberResponses.CreateSellerResponse> createSeller(@RequestBody MemberRequests.CreateSellerRequest request) {
        return ApiResponse.ok("seller created", memberCommandService.createSeller(request));
    }

    @PostMapping("/member/admin/companies")
    public MemberResponses.CreateCompanyResponse createCompany(@RequestBody MemberRequests.CreateCompanyRequest request) {
        return memberCommandService.createCompany(request);
    }

    @PostMapping("/member/admin/users")
    public MemberResponses.CreateAdminUserResponse createAdminUser(@RequestBody MemberRequests.CreateAdminUserRequest request) {
        return memberCommandService.createAdminUser(request);
    }

    @PatchMapping("/member/admin/users/{id}")
    public MemberResponses.UpdateAdminUserResponse updateAdminUser(@PathVariable String id, @RequestBody MemberRequests.UpdateAdminUserRequest request) {
        return memberCommandService.updateAdminUser(id, request);
    }

    @PatchMapping("/member/roles/{roleId}/permissions")
    public ApiResponse<MemberResponses.RolePermissionUpdateResponse> updateRolePermissions(@PathVariable String roleId,
                                                                                            @RequestBody MemberRequests.UpdateRolePermissionsRequest request,
                                                                                            @RequestHeader(value = "X-Invoker-Account-Id", required = false) String changedBy) {
        return ApiResponse.ok("role permission updated", memberCommandService.updateRolePermissions(roleId, request, changedBy));
    }
}
