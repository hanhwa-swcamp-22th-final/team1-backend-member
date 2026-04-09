package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.CreateDirectUserRequest;
import com.conk.member.command.application.dto.response.CreateDirectUserResponse;
import com.conk.member.command.application.dto.response.SimpleUserStatusResponse;
import com.conk.member.command.application.service.UserService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<CreateDirectUserResponse>> createDirect(@RequestBody CreateDirectUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("worker created", userService.createDirect(request)));
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> deactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user deactivated", userService.deactivate(userId)));
    }

    @PostMapping("/{userId}/reactivate")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> reactivate(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("user reactivated", userService.reactivate(userId)));
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<ApiResponse<SimpleUserStatusResponse>> resetPassword(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok("password reset", userService.resetPassword(userId)));
    }
}
