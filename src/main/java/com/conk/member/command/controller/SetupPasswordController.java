package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.SetupPasswordCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SetupPasswordController {

    private final SetupPasswordCommandService setupPasswordCommandService;

    public SetupPasswordController(SetupPasswordCommandService setupPasswordCommandService) {
        this.setupPasswordCommandService = setupPasswordCommandService;
    }

    @PostMapping("/member/auth/setup-password")
    public ResponseEntity<ApiResponse<SetupPasswordResponse>> setupPassword(@RequestBody SetupPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("setup password", setupPasswordCommandService.setupPassword(request)));
    }
}
