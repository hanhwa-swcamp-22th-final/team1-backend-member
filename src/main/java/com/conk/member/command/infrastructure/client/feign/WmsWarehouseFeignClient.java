package com.conk.member.command.infrastructure.client.feign;

import com.conk.member.common.config.FeignConfig;
import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.response.WmsWarehouseItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "wmsWarehouseFeignClient",
        url = "${warehouse.service.base-url}",
        configuration = FeignConfig.class
)
public interface WmsWarehouseFeignClient {

    @GetMapping("/wms/warehouses")
    ApiResponse<List<WmsWarehouseItem>> getWarehouses();

    @GetMapping("/wms/warehouses/{warehouseId}")
    ApiResponse<WmsWarehouseItem> getWarehouse(@PathVariable("warehouseId") String warehouseId);
}
