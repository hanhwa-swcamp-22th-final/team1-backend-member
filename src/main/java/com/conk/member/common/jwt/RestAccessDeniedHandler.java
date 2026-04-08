package com.conk.member.common.jwt;

/*
 * 인가 실패(403 Forbidden) 핸들러.
 * 인증은 됐지만 해당 리소스에 접근 권한이 없는 경우 JSON 형태로 응답한다.
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(
            "{\"success\":false,\"message\":\"" + accessDeniedException.getMessage() + "\",\"data\":null}"
        );
    }
}
