package com.conk.member.query.controller;

import com.conk.member.common.util.AdminPayloadCompat;
import com.conk.member.query.dto.AdminUserSummary;
import com.conk.member.query.dto.AdminUserListRequest;
import com.conk.member.query.service.AdminUserListQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdminUserListQueryController {

    private final AdminUserListQueryService adminUserListQueryService;

    public AdminUserListQueryController(AdminUserListQueryService adminUserListQueryService) {
        this.adminUserListQueryService = adminUserListQueryService;
    }

    @GetMapping("/member/admin/users")
    public Map<String, Object> getAdminUsers(@RequestParam(required = false, name = "companyId") String companyId,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false, name = "status") String status,
                                             @RequestParam(required = false) String keyword) {
        AdminUserListRequest request = new AdminUserListRequest();
        request.setCompanyId(companyId);
        request.setRole(role);
        request.setStatus(status);
        request.setKeyword(keyword);
        List<AdminUserSummary> users = adminUserListQueryService.getAdminUsers(request);
        return AdminPayloadCompat.items(users);
    }
}
