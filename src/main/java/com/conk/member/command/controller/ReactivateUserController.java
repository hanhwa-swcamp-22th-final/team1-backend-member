package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.ReactivateUserCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReactivateUserController {

    private final ReactivateUserCommandService reactivateUserCommandService;

    public ReactivateUserController(ReactivateUserCommandService reactivateUserCommandService) {
        this.reactivateUserCommandService = reactivateUserCommandService;
    }

    @PostMapping("/member/users/{userId}/reactivate")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> reactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user reactivated", reactivateUserCommandService.reactivate(userId)));
    }
}
