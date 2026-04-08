package com.conk.member.command.controller;

import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.DeactivateUserCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeactivateUserController {

    private final DeactivateUserCommandService deactivateUserCommandService;

    public DeactivateUserController(DeactivateUserCommandService deactivateUserCommandService) {
        this.deactivateUserCommandService = deactivateUserCommandService;
    }

    @PostMapping("/member/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> deactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user deactivated", deactivateUserCommandService.deactivate(userId)));
    }
}
