package com.conk.member.command.controller;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.command.controller.dto.ApiResponse;
import com.conk.member.command.controller.dto.LoginApiData;
import com.conk.member.command.controller.dto.LoginUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginApiData>> login(@RequestBody LoginRequest request) {
        LoginResponse serviceResponse = authService.login(request);

        LoginUserInfo userInfo = new LoginUserInfo(
                serviceResponse.getAccountId(),
                serviceResponse.getEmail(),
                serviceResponse.getName(),
                serviceResponse.getRole(),
                serviceResponse.getStatus(),
                serviceResponse.getOrganization()
        );

        LoginApiData data = new LoginApiData(
                serviceResponse.getAccessToken(),
                userInfo
        );

        return ResponseEntity.ok(ApiResponse.success("login", data));
}


}
