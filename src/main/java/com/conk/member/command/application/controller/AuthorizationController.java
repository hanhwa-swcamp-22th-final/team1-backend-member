package com.conk.member.command.application.controller;

/*
 * Nginx auth_request 서브요청을 처리하는 컨트롤러다.
 *
 * Nginx는 모든 protected 요청 전에 /_auth → /member/authorization 을 호출한다.
 * JWT가 유효하면 200 + 유저 식별 헤더를 반환하고,
 * Nginx가 그 헤더를 X-User-Id 등으로 백엔드에 전달한다.
 * JWT가 없거나 유효하지 않으면 Spring Security가 401을 반환한다.
 */

import com.conk.member.common.security.MemberUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/member")
public class AuthorizationController {

    @GetMapping("/authorization")
    public ResponseEntity<Void> authorize(@AuthenticationPrincipal MemberUserPrincipal principal) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        // HTTP 헤더는 Latin-1(0~255)만 허용 → 한글 이름은 Base64로 인코딩
        String encodedName = Base64.getEncoder()
                .encodeToString(principal.getUserName().getBytes(StandardCharsets.UTF_8));

        builder.header("X-User-Id",   principal.getAccountId());
        builder.header("X-User-Name", encodedName);
        builder.header("X-Role",      principal.getRoleName());

        if (StringUtils.hasText(principal.getSellerId())) {
            builder.header("X-Seller-Id", principal.getSellerId());
        }
        if (StringUtils.hasText(principal.getTenantId())) {
            builder.header("X-Tenant-Id", principal.getTenantId());
        }

        return builder.build();
    }
}
