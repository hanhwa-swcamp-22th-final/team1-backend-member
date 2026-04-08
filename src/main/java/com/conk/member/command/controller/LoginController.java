package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.LoginCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginCommandService loginCommandService;

    public LoginController(LoginCommandService loginCommandService) {
        this.loginCommandService = loginCommandService;
    }

    @PostMapping("/member/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("login", loginCommandService.login(request)));
    }
}
