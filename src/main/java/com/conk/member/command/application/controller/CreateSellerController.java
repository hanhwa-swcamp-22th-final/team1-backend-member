package com.conk.member.command.application.controller;

import com.conk.member.command.application.dto.request.CreateSellerRequest;
import com.conk.member.command.application.dto.response.CreateSellerResponse;
import com.conk.member.command.application.service.CreateSellerCommandService;
import com.conk.member.common.security.MemberUserPrincipal;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateSellerController {

    private final CreateSellerCommandService createSellerCommandService;

    public CreateSellerController(CreateSellerCommandService createSellerCommandService) {
        this.createSellerCommandService = createSellerCommandService;
    }

    @PostMapping("/member/sellers")
    public ResponseEntity<ApiResponse<CreateSellerResponse>> createSeller(
            @RequestBody CreateSellerRequest request,
            @AuthenticationPrincipal MemberUserPrincipal principal) {
        if (!StringUtils.hasText(request.getTenantId()) && principal != null) {
            request.setTenantId(principal.getTenantId());
        }
        return ResponseEntity.ok(ApiResponse.ok("seller created", createSellerCommandService.createSeller(request)));
    }
}
