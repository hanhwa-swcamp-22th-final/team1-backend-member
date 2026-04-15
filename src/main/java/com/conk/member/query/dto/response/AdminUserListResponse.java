package com.conk.member.query.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserListResponse {
    private String id;
    private String companyId;
    private String name;
    private String email;
    private String role;
    private String organization;
    private String sellerId;
    private String warehouseId;
    private String workerCode;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;

    public String getStatus() {
        if (status == null) {
            return null;
        }
        return switch (status.trim().toUpperCase()) {
            case "TEMP_PASSWORD" -> "INVITE_PENDING";
            default -> status;
        };
    }

    @JsonProperty("warehouse")
    public String getWarehouse() {
        return warehouseId;
    }
}
