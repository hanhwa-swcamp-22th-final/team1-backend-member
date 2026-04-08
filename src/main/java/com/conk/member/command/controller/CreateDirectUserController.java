package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.service.CreateDirectUserCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateDirectUserController {

    private final CreateDirectUserCommandService createDirectUserCommandService;

    public CreateDirectUserController(CreateDirectUserCommandService createDirectUserCommandService) {
        this.createDirectUserCommandService = createDirectUserCommandService;
    }

    @PostMapping("/member/users/direct")
    public ResponseEntity<ApiResponse<CreateDirectUserResponse>> createDirect(@RequestBody CreateDirectUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("worker created", createDirectUserCommandService.createDirect(request)));
    }
}
