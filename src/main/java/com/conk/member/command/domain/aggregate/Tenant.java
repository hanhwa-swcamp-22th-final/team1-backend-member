package com.conk.member.command.domain.aggregate;

/*
 * 3PL 업체 정보를 보관하는 엔티티다.
 */

import com.conk.member.command.domain.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant")
public class Tenant extends BaseAuditEntity {
    @Id
    @Column(name = "tenant_id")
    private String tenantId;
    @Column(name = "tenant_code", nullable = false, unique = true)
    private String tenantCode;
    @Column(name = "tenant_name", nullable = false)
    private String tenantName;
    @Column(name = "representative_name", nullable = false)
    private String representativeName;
    @Column(name = "business_no")
    private String businessNo;
    @Column(name = "phone_no")
    private String phoneNo;
    @Column(name = "email")
    private String email;
    @Column(name = "address")
    private String address;
    @Column(name = "tenant_type")
    private String tenantType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenantStatus status;
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    public void activate() {
        this.status = TenantStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }


    public static Tenant create(
            String tenantId,
            String tenantCode,
            String tenantName,
            String representativeName,
            String businessNo,
            String phoneNo,
            String email,
            String address,
            String tenantType,
            String createdBy
    ) {
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(tenantName);
        tenant.setRepresentativeName(representativeName);
        tenant.setBusinessNo(businessNo);
        tenant.setPhoneNo(phoneNo);
        tenant.setEmail(email);
        tenant.setAddress(address);
        tenant.setTenantType(tenantType);
        tenant.setStatus(TenantStatus.SETTING);
        tenant.setCreatedBy(createdBy);
        tenant.setUpdatedBy(createdBy);
        return tenant;
    }
}
