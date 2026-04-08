package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.UserSummary;
import com.conk.member.query.dto.UserListRequest;
import com.conk.member.query.service.UserListQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserListQueryController {

    private final UserListQueryService userListQueryService;

    public UserListQueryController(UserListQueryService userListQueryService) {
        this.userListQueryService = userListQueryService;
    }

    @GetMapping("/member/users")
    public ApiResponse<List<UserSummary>> getUsers(UserListRequest request) {
        return ApiResponse.ok("user list", userListQueryService.getUsers(request));
    }
}
