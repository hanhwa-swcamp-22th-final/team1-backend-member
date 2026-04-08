package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.RefreshTokenCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshTokenController {

    private final RefreshTokenCommandService refreshTokenCommandService;

    public RefreshTokenController(RefreshTokenCommandService refreshTokenCommandService) {
        this.refreshTokenCommandService = refreshTokenCommandService;
    }

    @PostMapping("/member/auth/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader("Authorization") String authorization) {
        String refreshToken = CommandControllerSupport.extractBearerToken(authorization);
        return ResponseEntity.ok(ApiResponse.ok("token refreshed", refreshTokenCommandService.refreshToken(refreshToken)));
    }
}
