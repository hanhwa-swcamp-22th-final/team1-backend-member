package com.conk.member.command.controller;

import com.conk.member.command.controller.dto.ApiResponse;
import com.conk.member.common.MemberApiMockDataFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/member/users")
public class UserCommandController {

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetUserPassword(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("resetUserPassword", MemberApiMockDataFactory.accountStatus("ACTIVE")));
    }
}
