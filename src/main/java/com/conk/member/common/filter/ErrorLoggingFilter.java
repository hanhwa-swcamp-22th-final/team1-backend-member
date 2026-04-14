package com.conk.member.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 필터 체인 전체를 감싸서 처리되지 않은 예외를 ERROR 레벨로 로깅한다.
 * GlobalExceptionHandler가 잡지 못하는 필터 레이어 예외를 추적하기 위해 사용한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ErrorLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[FilterChain 예외] {} {} → {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage(), e);
            throw e;
        }
    }
}
