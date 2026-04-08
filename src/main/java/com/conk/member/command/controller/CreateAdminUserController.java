package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.application.service.CreateAdminUserCommandService;
import com.conk.member.common.util.AdminPayloadCompat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CreateAdminUserController {

    private final CreateAdminUserCommandService createAdminUserCommandService;

    public CreateAdminUserController(CreateAdminUserCommandService createAdminUserCommandService) {
        this.createAdminUserCommandService = createAdminUserCommandService;
    }

    @PostMapping("/member/admin/users")
    public Map<String, Object> createAdminUser(@RequestBody CreateAdminUserRequest request) {
        CreateAdminUserResponse response = createAdminUserCommandService.createAdminUser(request);
        return AdminPayloadCompat.raw(response);
    }
}
