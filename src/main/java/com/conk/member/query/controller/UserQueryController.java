package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserQueryController {

    private final MemberQueryService memberQueryService;

    public UserQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
    }

    @GetMapping("/member/users")
    public ApiResponse<List<QueryResponses.UserSummary>> getUsers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(
                "user list",
                memberQueryService.getUsers(tenantId, role, accountStatus, sellerId, warehouseId, keyword)
        );
    }
}
