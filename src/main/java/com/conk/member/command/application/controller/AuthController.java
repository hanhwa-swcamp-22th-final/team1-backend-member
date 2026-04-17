package com.conk.member.command.application.controller;

import com.conk.member.command.application.dto.request.InviteAccountRequest;
import com.conk.member.command.application.dto.request.LoginRequest;
import com.conk.member.command.application.dto.request.ChangePasswordRequest;
import com.conk.member.command.application.dto.request.SetupPasswordRequest;
import com.conk.member.command.application.dto.response.ChangePasswordResponse;
import com.conk.member.command.application.dto.response.InviteAccountResponse;
import com.conk.member.command.application.dto.response.LoginResponse;
import com.conk.member.command.application.dto.response.SetupPasswordResponse;
import com.conk.member.command.application.service.AuthService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.common.security.MemberUserPrincipal;
import com.conk.member.common.util.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/member/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return buildTokenResponse("login", response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal MemberUserPrincipal principal) {
        authService.logout(principal == null ? null : principal.getAccountId());
        ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.ok("logged out", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("refresh token cookie is missing", null));
        }

        LoginResponse response = authService.refreshToken(refreshToken);
        return buildTokenResponse("token refreshed", response);
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<InviteAccountResponse>> invite(
            @RequestBody InviteAccountRequest request,
            @AuthenticationPrincipal MemberUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                "invite sent",
                authService.invite(
                        request,
                        principal == null ? null : principal.getAccountId(),
                        principal == null ? null : principal.getTenantId()  // JwtAuthenticationFilter가 JWT에서 파싱한 값
                )
        ));
    }

    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<SetupPasswordResponse>> setupPassword(@RequestBody SetupPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("setup password", authService.setupPassword(request)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @AuthenticationPrincipal MemberUserPrincipal principal,
            @RequestBody ChangePasswordRequest request) {
        if (principal == null) {
            throw new MemberException(ErrorCode.UNAUTHORIZED);
        }

        return ResponseEntity.ok(ApiResponse.ok(
                "password changed",
                authService.changePassword(principal.getAccountId(), request)
        ));
    }

    private ResponseEntity<ApiResponse<LoginResponse>> buildTokenResponse(String message, LoginResponse response) {
        ResponseCookie refreshCookie = createRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.ok(message, response));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(authService.getRefreshExpiration()).getSeconds())
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie createDeleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}
