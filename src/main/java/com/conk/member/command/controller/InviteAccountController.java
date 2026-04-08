package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.service.InviteAccountCommandService;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InviteAccountController {

    private final InviteAccountCommandService inviteAccountCommandService;

    public InviteAccountController(InviteAccountCommandService inviteAccountCommandService) {
        this.inviteAccountCommandService = inviteAccountCommandService;
    }

    @PostMapping("/member/auth/invite")
    public ResponseEntity<ApiResponse<InviteAccountResponse>> invite(@RequestBody InviteAccountRequest request,
                                                                     Authentication authentication,
                                                                     @RequestHeader(value = "X-Invoker-Account-Id", required = false) String invokerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                "invite sent",
                inviteAccountCommandService.invite(request, CommandControllerSupport.resolveAccountId(authentication, invokerId))
        ));
    }
}
