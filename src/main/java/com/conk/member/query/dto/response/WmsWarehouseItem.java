package com.conk.member.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * wms-service GET /wms/warehouses 응답의 각 창고 항목을 파싱하기 위한 내부 DTO다.
 * FAIL_ON_UNKNOWN_PROPERTIES = false 설정으로 불필요한 필드는 무시한다.
 */
@Getter
@NoArgsConstructor
public class WmsWarehouseItem {
    private String id;
    private String code;
    private String name;
    private String status;
}
