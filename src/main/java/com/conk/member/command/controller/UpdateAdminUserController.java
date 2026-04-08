package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.UpdateAdminUserRequest;
import com.conk.member.command.application.dto.response.UpdateAdminUserResponse;
import com.conk.member.command.application.service.UpdateAdminUserCommandService;
import com.conk.member.common.util.AdminPayloadCompat;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateAdminUserController {

    private final UpdateAdminUserCommandService updateAdminUserCommandService;

    public UpdateAdminUserController(UpdateAdminUserCommandService updateAdminUserCommandService) {
        this.updateAdminUserCommandService = updateAdminUserCommandService;
    }

    @PatchMapping("/member/admin/users/{id}")
    public Map<String, Object> updateAdminUser(@PathVariable String id,
                                               @RequestBody UpdateAdminUserRequest request) {
        UpdateAdminUserResponse response = updateAdminUserCommandService.updateAdminUser(id, request);
        return AdminPayloadCompat.raw(response);
    }
}
