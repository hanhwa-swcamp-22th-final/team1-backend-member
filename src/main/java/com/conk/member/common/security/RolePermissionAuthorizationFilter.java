package com.conk.member.common.security;

import com.conk.member.common.jwt.RestAccessDeniedHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RolePermissionAuthorizationFilter extends OncePerRequestFilter {

    private final RolePermissionAuthorizationService authorizationService;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public RolePermissionAuthorizationFilter(RolePermissionAuthorizationService authorizationService,
                                             RestAccessDeniedHandler accessDeniedHandler) {
        this.authorizationService = authorizationService;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/member/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationService.isAllowed(authentication.getName(), requestPath, request.getMethod())) {
            accessDeniedHandler.handle(request, response, new AccessDeniedException("권한이 없습니다."));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
