package com.conk.member.command.infrastructure.service;

import com.conk.member.query.dto.response.WmsWarehouseItem;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * wms-service의 GET /wms/warehouses 를 내부 호출로 사용하는 HTTP 클라이언트다.
 *
 * <p>Nginx /_auth 검증을 우회하기 위해 {@code X-Internal-Call: true} 헤더를 포함한다.
 * wms-service의 AuthContextResolver는 {@code X-Tenant-Id} 헤더를 읽어 tenantId를 주입하므로,
 * JWT 없이도 창고 목록을 조회할 수 있다.
 *
 * <p>wms-service 장애 또는 응답 오류 시 빈 리스트를 반환해 서비스 전체가 중단되지 않도록 한다.
 */
@Service
public class WmsWarehouseClient {

    private static final Logger log = LoggerFactory.getLogger(WmsWarehouseClient.class);

    private static final String HEADER_INTERNAL_CALL = "X-Internal-Call";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String wmsBaseUrl;

    public WmsWarehouseClient(
            @Value("${warehouse.service.base-url:}") String wmsBaseUrl
    ) {
        this.wmsBaseUrl = wmsBaseUrl;
    }

    /**
     * tenantId에 속한 창고 목록을 wms-service에서 조회한다.
     *
     * @param tenantId 조회 대상 테넌트 ID
     * @return 창고 목록 (wms-service 호출 실패 시 빈 리스트)
     */
    public List<WmsWarehouseItem> findWarehousesByTenantId(String tenantId) {
        if (!StringUtils.hasText(wmsBaseUrl) || !StringUtils.hasText(tenantId)) {
            return List.of();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(wmsBaseUrl) + "/wms/warehouses"))
                .header(HEADER_INTERNAL_CALL, "true")
                .header(HEADER_TENANT_ID, tenantId)
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("WMS 창고 목록 조회 실패: tenantId={} status={}", tenantId, response.statusCode());
                return List.of();
            }

            // wms-service ApiResponse 구조: { "success": true, "data": [...] }
            JsonNode dataNode = objectMapper.readTree(response.body()).path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                return List.of();
            }

            return objectMapper.convertValue(
                    dataNode,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, WmsWarehouseItem.class)
            );

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("WMS 창고 목록 조회 중 오류 발생: tenantId={} message={}", tenantId, e.getMessage());
            return List.of();
        }
    }

    private String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
