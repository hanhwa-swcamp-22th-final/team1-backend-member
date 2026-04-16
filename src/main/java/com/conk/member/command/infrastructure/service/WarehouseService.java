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

@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private final WmsWarehouseFeignClient wmsWarehouseFeignClient;
    private final String warehouseBaseUrl;

    public WarehouseService(WmsWarehouseFeignClient wmsWarehouseFeignClient,
                            @Value("${warehouse.service.base-url:}") String warehouseBaseUrl) {
        this.wmsWarehouseFeignClient = wmsWarehouseFeignClient;
        this.warehouseBaseUrl = warehouseBaseUrl;
    }

    public boolean exists(String warehouseId) {
        if (!StringUtils.hasText(warehouseId)) {
            return false;
        }

        if (!StringUtils.hasText(warehouseBaseUrl)) {
            return true;
        }

        try {
            ApiResponse<WmsWarehouseItem> response = wmsWarehouseFeignClient.getWarehouse(warehouseId);
            return response != null && response.isSuccess() && response.getData() != null;
        } catch (FeignException.NotFound exception) {
            return false;
        } catch (FeignException exception) {
            log.warn("창고 서비스 검증에 실패해 기본 검증으로 대체합니다. warehouseId={} message={}", warehouseId, exception.getMessage());
            return true;
        }
    }
}
