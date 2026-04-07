package com.conk.member.command.controller;

/*
 * command 성격의 요청을 받는 컨트롤러다.
 * 컨트롤러는 요청을 받고 서비스에 전달한 뒤 응답만 돌려준다.
 */

import com.conk.member.command.application.dto.request.MemberRequests;
import com.conk.member.command.application.dto.response.MemberResponses;
import com.conk.member.command.application.service.AuthTokenService;
import com.conk.member.command.application.service.MemberCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberCommandController {

    private final MemberCommandService memberCommandService;
    private final AuthTokenService authTokenService;

    public MemberCommandController(MemberCommandService memberCommandService,
                                   AuthTokenService authTokenService) {
        this.memberCommandService = memberCommandService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/member/auth/login")
    public ResponseEntity<ApiResponse<MemberResponses.LoginResponse>> login(@RequestBody MemberRequests.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("login", memberCommandService.login(request)));
    }

    @PostMapping("/member/auth/refresh")
    public ResponseEntity<ApiResponse<MemberResponses.LoginResponse>> refresh(
            @RequestHeader("Authorization") String authorization) {
        String refreshToken = extractBearerToken(authorization);
        return ResponseEntity.ok(ApiResponse.ok("token refreshed", authTokenService.refreshToken(refreshToken)));
    }

    @PostMapping("/member/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        String refreshToken = extractBearerToken(authorization);
        authTokenService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("logged out", null));
    }

    @PostMapping("/member/auth/setup-password")
    public ResponseEntity<ApiResponse<MemberResponses.SetupPasswordResponse>> setupPassword(
            @RequestBody MemberRequests.SetupPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("setup password", memberCommandService.setupPassword(request)));
    }

    @PostMapping("/member/auth/invite")
    public ResponseEntity<ApiResponse<MemberResponses.InviteAccountResponse>> invite(
            @RequestBody MemberRequests.InviteAccountRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Invoker-Account-Id", required = false) String invokerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "invite sent",
                memberCommandService.invite(request, resolveAccountId(authentication, invokerId))
        ));
    }

    @PostMapping("/member/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse<MemberResponses.SimpleUserStatusResponse>> resetPassword(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("password reset", memberCommandService.resetPassword(userId)));
    }

    @PostMapping("/member/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<MemberResponses.SimpleUserStatusResponse>> deactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user deactivated", memberCommandService.deactivate(userId)));
    }

    @PostMapping("/member/users/{userId}/reactivate")
    public ResponseEntity<ApiResponse<MemberResponses.SimpleUserStatusResponse>> reactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user reactivated", memberCommandService.reactivate(userId)));
    }

    @PostMapping("/member/users/direct")
    public ResponseEntity<ApiResponse<MemberResponses.CreateDirectUserResponse>> createDirect(
            @RequestBody MemberRequests.CreateDirectUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("worker created", memberCommandService.createDirect(request)));
    }

    @PostMapping("/member/sellers")
    public ResponseEntity<ApiResponse<MemberResponses.CreateSellerResponse>> createSeller(
            @RequestBody MemberRequests.CreateSellerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("seller created", memberCommandService.createSeller(request)));
    }

    @PostMapping("/member/admin/companies")
    public ResponseEntity<ApiResponse<MemberResponses.CreateCompanyResponse>> createCompany(
            @RequestBody MemberRequests.CreateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("company created", memberCommandService.createCompany(request)));
    }

    @PatchMapping("/member/admin/companies/{id}")
    public ResponseEntity<ApiResponse<MemberResponses.UpdateCompanyResponse>> updateCompany(
            @PathVariable String id,
            @RequestBody MemberRequests.UpdateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("company updated", memberCommandService.updateCompany(id, request)));
    }

    @PostMapping("/member/admin/users")
    public ResponseEntity<ApiResponse<MemberResponses.CreateAdminUserResponse>> createAdminUser(
            @RequestBody MemberRequests.CreateAdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("admin user created", memberCommandService.createAdminUser(request)));
    }

    @PatchMapping("/member/admin/users/{id}")
    public ResponseEntity<ApiResponse<MemberResponses.UpdateAdminUserResponse>> updateAdminUser(
            @PathVariable String id,
            @RequestBody MemberRequests.UpdateAdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("admin user updated", memberCommandService.updateAdminUser(id, request)));
    }

    @PatchMapping("/member/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<MemberResponses.RolePermissionUpdateResponse>> updateRolePermissions(
            @PathVariable String roleId,
            @RequestBody MemberRequests.UpdateRolePermissionsRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Invoker-Account-Id", required = false) String changedBy) {
        return ResponseEntity.ok(ApiResponse.ok(
                "role permissions updated",
                memberCommandService.updateRolePermissions(
                        roleId,
                        request,
                        resolveAccountId(authentication, changedBy)
                )
        ));
    }

    private String resolveAccountId(Authentication authentication, String fallbackAccountId) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return fallbackAccountId;
    }

    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
