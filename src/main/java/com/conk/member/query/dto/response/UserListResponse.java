package com.conk.member.query.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserListResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String accountStatus;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String workerCode;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    @JsonProperty("seller")
    public String getSeller() {
        return sellerId;
    }

    @JsonProperty("warehouse")
    public String getWarehouse() {
        return warehouseId;
    }
}
