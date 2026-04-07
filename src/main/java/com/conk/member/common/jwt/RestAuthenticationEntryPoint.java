package com.conk.member.common.jwt;

/*
 * 인증 실패(401 Unauthorized) 핸들러.
 * 토큰 없음, 토큰 만료, 토큰 위변조 등 인증 오류 시 JSON 형태로 응답한다.
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
            "{\"success\":false,\"message\":\"" + authException.getMessage() + "\",\"data\":null}"
        );
    }
}
