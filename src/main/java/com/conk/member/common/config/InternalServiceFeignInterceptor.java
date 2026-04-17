package com.conk.member.common.config;

import com.conk.member.common.security.MemberUserPrincipal;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * 내부 서비스 호출에 필요한 식별 헤더를 공통으로 주입한다.
 */
@Configuration
public class InternalServiceFeignInterceptor {

    private static final String HEADER_USER_ID = "X-USER-ID";
    private static final String HEADER_USER_NAME = "X-USER-NAME";
    private static final String HEADER_SELLER_ID = "X-SELLER-ID";
    private static final String HEADER_TENANT_ID = "X-TENANT-ID";
    private static final String HEADER_ROLE = "X-ROLE";

    @Bean
    public RequestInterceptor internalServiceRequestInterceptor() {
        return this::applyHeaders;
    }

    private void applyHeaders(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberUserPrincipal principal)) {
            return;
        }

        addHeaderIfPresent(template, HEADER_USER_ID, principal.getAccountId());
        addHeaderIfPresent(template, HEADER_USER_NAME, principal.getUserName());
        addHeaderIfPresent(template, HEADER_SELLER_ID, principal.getSellerId());
        addHeaderIfPresent(template, HEADER_TENANT_ID, principal.getTenantId());
        addHeaderIfPresent(template, HEADER_ROLE, principal.getRoleName());
    }

    private void addHeaderIfPresent(RequestTemplate template, String headerName, String headerValue) {
        if (StringUtils.hasText(headerValue)) {
            template.header(headerName, headerValue);
        }
    }
}
