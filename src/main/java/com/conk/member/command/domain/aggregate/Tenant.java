package com.conk.member.command.domain.aggregate;

import com.conk.member.command.domain.enums.TenantStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant")
public class Tenant {

  @Id
  @Column(name = "tenant_id", length = 255)
  private String tenantId;

  @Column(name = "tenant", nullable = false, length = 255, unique = true)
  private String tenant;

  @Column(name = "tenant_name", nullable = false, length = 255)
  private String tenantName;

  @Column(name = "representative_name", length = 255)
  private String representativeName;

  @Column(name = "business_no", length = 255)
  private String businessNo;

  @Column(name = "phone_no", length = 255)
  private String phoneNo;

  @Column(name = "email", length = 255)
  private String email;

  @Column(name = "address", length = 255)
  private String address;

  @Column(name = "tenant_type", length = 50)
  private String tenantType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private TenantStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "updated_by", length = 255)
  private String updatedBy;

  @Column(name = "activated_at")
  private LocalDateTime activatedAt;

  protected Tenant() {
  }

  private Tenant(
      String tenantId,
      String tenant,
      String tenantName,
      String representativeName,
      String businessNo,
      String phoneNo,
      String email,
      String address,
      String tenantType,
      String createdBy
  ) {
    this.tenantId = tenantId;
    this.tenant = tenant;
    this.tenantName = tenantName;
    this.representativeName = representativeName;
    this.businessNo = businessNo;
    this.phoneNo = phoneNo;
    this.email = email;
    this.address = address;
    this.tenantType = tenantType;
    this.status = TenantStatus.SETTING;
    this.createdBy = createdBy;
  }

  public static Tenant create(
      String tenantId,
      String tenant,
      String tenantName,
      String representativeName,
      String businessNo,
      String phoneNo,
      String email,
      String address,
      String tenantType,
      String createdBy
  ) {
    return new Tenant(
        tenantId,
        tenant,
        tenantName,
        representativeName,
        businessNo,
        phoneNo,
        email,
        address,
        tenantType,
        createdBy
    );
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void activate(String updatedBy) {
    this.status = TenantStatus.ACTIVE;
    this.activatedAt = LocalDateTime.now();
    this.updatedBy = updatedBy;
  }

  public void inactivate(String updatedBy) {
    this.status = TenantStatus.INACTIVE;
    this.updatedBy = updatedBy;
  }

  public void changeInfo(
      String tenantName,
      String representativeName,
      String businessNo,
      String phoneNo,
      String email,
      String address,
      String tenantType,
      String updatedBy
  ) {
    this.tenantName = tenantName;
    this.representativeName = representativeName;
    this.businessNo = businessNo;
    this.phoneNo = phoneNo;
    this.email = email;
    this.address = address;
    this.tenantType = tenantType;
    this.updatedBy = updatedBy;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTenant() {
    return tenant;
  }

  public String getTenantName() {
    return tenantName;
  }

  public String getRepresentativeName() {
    return representativeName;
  }

  public String getBusinessNo() {
    return businessNo;
  }

  public String getPhoneNo() {
    return phoneNo;
  }

  public String getEmail() {
    return email;
  }

  public String getAddress() {
    return address;
  }

  public String getTenantType() {
    return tenantType;
  }

  public TenantStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public LocalDateTime getActivatedAt() {
    return activatedAt;
  }
}