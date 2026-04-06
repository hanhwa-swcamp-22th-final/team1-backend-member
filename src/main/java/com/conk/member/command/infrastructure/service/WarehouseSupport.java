package com.conk.member.command.infrastructure.service;

/*
 * 외부 창고 서비스 MSA를 대신하는 학습용 검증 지원 서비스다.
 */

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WarehouseSupport {
    public boolean exists(String warehouseId) {
        return StringUtils.hasText(warehouseId);
    }
}
