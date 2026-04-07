package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.service.MemberQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminUserQueryController {

    private final MemberQueryService memberQueryService;

    public AdminUserQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
    }

    @GetMapping("/member/admin/users")
    public ApiResponse<List<QueryResponses.AdminUserSummary>> getAdminUsers(
            @RequestParam(required = false, name = "companyId") String companyId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(
                "admin user list",
                memberQueryService.getAdminUsers(companyId, role, status, keyword)
        );
    }
}
