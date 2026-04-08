package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.ResetPasswordCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResetPasswordController {

    private final ResetPasswordCommandService resetPasswordCommandService;

    public ResetPasswordController(ResetPasswordCommandService resetPasswordCommandService) {
        this.resetPasswordCommandService = resetPasswordCommandService;
    }

    @PostMapping("/member/users/{userId}/reset-password")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> resetPassword(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("password reset", resetPasswordCommandService.resetPassword(userId)));
    }
}
