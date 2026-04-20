package com.conk.member.common.jwt;

/*
 * 요청마다 Authorization 헤더를 확인해서 JWT 인증을 처리하는 필터다.
 * 토큰이 유효하면 SecurityContext에 인증 정보를 저장하고,
 * 다음 서비스가 사용할 수 있도록 사용자 식별 헤더도 요청에 주입한다.
 */

import com.conk.member.common.http.MutableHttpServletRequest;
import com.conk.member.common.security.MemberUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-USER-ID";
    private static final String HEADER_WORKER_CODE = "X-WORKER-CODE";
    private static final String HEADER_USER_NAME = "X-USER-NAME";
    private static final String HEADER_SELLER_ID = "X-SELLER-ID";
    private static final String HEADER_TENANT_ID = "X-TENANT-ID";
    private static final String HEADER_ROLE = "X-ROLE";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest requestToUse = request;

        try {
            String token = extractBearerToken(request);

            if (StringUtils.hasText(token)) {
                jwtTokenProvider.validateAccessToken(token);
                String accountId = jwtTokenProvider.getAccountIdFromJWT(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(accountId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                requestToUse = applyIdentityHeaders(request, userDetails);
            }
        } catch (Exception exception) {
            log.debug("JWT 인증 처리 중 오류가 발생했습니다. message={}", exception.getMessage());
        }

        filterChain.doFilter(requestToUse, response);
    }

    private HttpServletRequest applyIdentityHeaders(HttpServletRequest request, UserDetails userDetails) {
        MutableHttpServletRequest wrappedRequest = new MutableHttpServletRequest(request);

        if (userDetails instanceof MemberUserPrincipal principal) {
            addHeaderIfPresent(wrappedRequest, HEADER_USER_ID, principal.getAccountId());
            addHeaderIfPresent(wrappedRequest, HEADER_WORKER_CODE, principal.getWorkerCode());
            addHeaderIfPresent(wrappedRequest, HEADER_USER_NAME, principal.getUserName());
            addHeaderIfPresent(wrappedRequest, HEADER_SELLER_ID, principal.getSellerId());
            addHeaderIfPresent(wrappedRequest, HEADER_TENANT_ID, principal.getTenantId());
            addHeaderIfPresent(wrappedRequest, HEADER_ROLE, principal.getRoleName());
        }

        return wrappedRequest;
    }

    private void addHeaderIfPresent(MutableHttpServletRequest request, String headerName, String value) {
        if (StringUtils.hasText(value)) {
            request.putHeader(headerName, value);
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return null;
    }
}
