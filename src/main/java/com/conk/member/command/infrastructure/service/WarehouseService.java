package com.conk.member.command.infrastructure.service;

import com.conk.member.common.security.MemberUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private static final String HEADER_USER_ID = "X-USER-ID";
    private static final String HEADER_USER_NAME = "X-USER-NAME";
    private static final String HEADER_SELLER_ID = "X-SELLER-ID";
    private static final String HEADER_TENANT_ID = "X-TENANT-ID";
    private static final String HEADER_ROLE = "X-ROLE";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String warehouseBaseUrl;

    public WarehouseService(@Value("${warehouse.service.base-url:}") String warehouseBaseUrl) {
        this.warehouseBaseUrl = warehouseBaseUrl;
    }

    public boolean exists(String warehouseId) {
        if (!StringUtils.hasText(warehouseId)) {
            return false;
        }

        if (!StringUtils.hasText(warehouseBaseUrl)) {
            return true;
        }

        String encodedId = URLEncoder.encode(warehouseId, StandardCharsets.UTF_8);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(warehouseBaseUrl) + "/warehouses/" + encodedId))
                .GET();

        copyIdentityHeaders(builder);

        HttpRequest request = builder.build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("창고 서비스 검증에 실패해 기본 검증으로 대체합니다. warehouseId={} message={}", warehouseId, exception.getMessage());
            return true;
        }
    }

    private void copyIdentityHeaders(HttpRequest.Builder builder) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberUserPrincipal principal)) {
            return;
        }

        addHeaderIfPresent(builder, HEADER_USER_ID, principal.getAccountId());
        addHeaderIfPresent(builder, HEADER_USER_NAME, principal.getUserName());
        addHeaderIfPresent(builder, HEADER_SELLER_ID, principal.getSellerId());
        addHeaderIfPresent(builder, HEADER_TENANT_ID, principal.getTenantId());
        addHeaderIfPresent(builder, HEADER_ROLE, principal.getRoleName());
    }

    private void addHeaderIfPresent(HttpRequest.Builder builder, String headerName, String headerValue) {
        if (StringUtils.hasText(headerValue)) {
            builder.header(headerName, headerValue);
        }
    }

    private String trimSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
