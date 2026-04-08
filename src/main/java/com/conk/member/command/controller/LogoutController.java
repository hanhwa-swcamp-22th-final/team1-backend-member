package com.conk.member.command.controller;

import com.conk.member.command.application.service.LogoutCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private final LogoutCommandService logoutCommandService;

    public LogoutController(LogoutCommandService logoutCommandService) {
        this.logoutCommandService = logoutCommandService;
    }

    @PostMapping("/member/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        String refreshToken = CommandControllerSupport.extractBearerToken(authorization);
        logoutCommandService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("logged out", null));
    }
}
