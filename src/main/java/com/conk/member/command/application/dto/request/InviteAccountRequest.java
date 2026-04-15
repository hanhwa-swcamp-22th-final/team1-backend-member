package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteAccountRequest {
    private String role;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String name;
    private String email;

    @JsonAlias("organizationId")
    private String genericOrganizationId;

    @JsonAlias({"employeeNumber", "workerCode"})
    private String employeeNumber;

    public String getSellerId() {
        if (StringUtils.hasText(sellerId)) {
            return sellerId;
        }
        if (StringUtils.hasText(genericOrganizationId) && "SELLER".equalsIgnoreCase(role)) {
            return genericOrganizationId;
        }
        return sellerId;
    }

    public String getWarehouseId() {
        if (StringUtils.hasText(warehouseId)) {
            return warehouseId;
        }
        if (StringUtils.hasText(genericOrganizationId)
                && ("WH_MANAGER".equalsIgnoreCase(role)
                || "WAREHOUSE_MANAGER".equalsIgnoreCase(role)
                || "WH_WORKER".equalsIgnoreCase(role)
                || "WAREHOUSE_WORKER".equalsIgnoreCase(role))) {
            return genericOrganizationId;
        }
        return warehouseId;
    }
}
