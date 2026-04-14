package com.conk.member.command.domain.enums;

/*
 * 명세서/ERD에 나온 상태값 또는 역할값을 코드에서 안전하게 사용하기 위한 enum이다.
 */

public enum RoleName {
    SYSTEM_ADMIN,
    MASTER_ADMIN,
    WH_MANAGER,
    WH_WORKER,
    SELLER;

    public static RoleName fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("RoleName value must not be null.");
        }

        return switch (value.trim().toUpperCase()) {
            case "WAREHOUSE_MANAGER", "WH_MANAGER" -> WH_MANAGER;
            case "WAREHOUSE_WORKER", "WH_WORKER" -> WH_WORKER;
            default -> RoleName.valueOf(value.trim().toUpperCase());
        };
    }

    public boolean isWarehouseManager() {
        return this == WH_MANAGER;
    }

    public boolean isWarehouseWorker() {
        return this == WH_WORKER;
    }
}
