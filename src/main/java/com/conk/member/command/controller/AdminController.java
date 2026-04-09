package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.CreateAdminUserRequest;
import com.conk.member.command.application.dto.request.CreateCompanyRequest;
import com.conk.member.command.application.dto.request.UpdateAdminUserRequest;
import com.conk.member.command.application.dto.request.UpdateCompanyRequest;
import com.conk.member.command.application.dto.response.CreateAdminUserResponse;
import com.conk.member.command.application.dto.response.CreateCompanyResponse;
import com.conk.member.command.application.dto.response.UpdateAdminUserResponse;
import com.conk.member.command.application.dto.response.UpdateCompanyResponse;
import com.conk.member.command.application.service.AdminService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<CreateAdminUserResponse>> createAdminUser(
            @RequestBody CreateAdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("admin user created", adminService.createAdminUser(request)));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UpdateAdminUserResponse>> updateAdminUser(
            @PathVariable String id,
            @RequestBody UpdateAdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("admin user updated", adminService.updateAdminUser(id, request)));
    }

    @PostMapping("/companies")
    public ResponseEntity<ApiResponse<CreateCompanyResponse>> createCompany(
            @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("company created", adminService.createCompany(request)));
    }

    @PatchMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<UpdateCompanyResponse>> updateCompany(
            @PathVariable String id,
            @RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("company updated", adminService.updateCompany(id, request)));
    }
}
