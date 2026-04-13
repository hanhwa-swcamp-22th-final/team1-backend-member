package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.UserListRequest;
import com.conk.member.query.dto.response.UserListResponse;
import com.conk.member.query.service.UserQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/member/users")
public class UserQueryController {

    private final UserQueryService userQueryService;

    public UserQueryController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping
    public ApiResponse<List<UserListResponse>> getUsers(UserListRequest request) {
        return ApiResponse.ok("user list", userQueryService.getUsers(request));
    }
}
