package com.conk.member.command.infrastructure.service;

/*
 * 외부 창고 서비스를 대신하는 학습용 서비스다.
 * 지금은 warehouseId 값이 비어있지 않은지만 간단히 확인한다.
 */

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WarehouseService {

    public boolean exists(String warehouseId) {
        return StringUtils.hasText(warehouseId);
    }
}
