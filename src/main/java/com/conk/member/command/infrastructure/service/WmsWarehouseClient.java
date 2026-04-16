package com.conk.member.command.infrastructure.service;

import com.conk.member.command.infrastructure.client.feign.WmsWarehouseFeignClient;
import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.response.WmsWarehouseItem;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private final WmsWarehouseFeignClient wmsWarehouseFeignClient;
    private final String wmsBaseUrl;

    public WmsWarehouseClient(
            WmsWarehouseFeignClient wmsWarehouseFeignClient,
            @Value("${warehouse.service.base-url:}") String wmsBaseUrl
    ) {
        this.wmsWarehouseFeignClient = wmsWarehouseFeignClient;
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

        try {
            ApiResponse<List<WmsWarehouseItem>> response = wmsWarehouseFeignClient.getWarehouses();
            if (response == null || !response.isSuccess() || response.getData() == null) {
                return List.of();
            }
            return response.getData();
        } catch (FeignException exception) {
            log.warn("WMS 창고 목록 조회 중 오류 발생: tenantId={} message={}", tenantId, exception.getMessage());
            return List.of();
        }
    }
}
