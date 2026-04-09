package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("login", authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(CommandControllerSupport.extractBearerToken(authorization));
        return ResponseEntity.ok(ApiResponse.ok("logged out", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(ApiResponse.ok("token refreshed",
                authService.refreshToken(CommandControllerSupport.extractBearerToken(authorization))));
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<InviteAccountResponse>> invite(
            @RequestBody InviteAccountRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Invoker-Account-Id", required = false) String invokerId) {
        return ResponseEntity.ok(ApiResponse.ok("invite sent",
                authService.invite(request, CommandControllerSupport.resolveAccountId(authentication, invokerId))));
    }

    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<SetupPasswordResponse>> setupPassword(@RequestBody SetupPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("setup password", authService.setupPassword(request)));
    }
}
