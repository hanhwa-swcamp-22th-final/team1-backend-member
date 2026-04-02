package com.conk.member.command.domain.aggregate;

import com.conk.member.command.domain.enums.AccountStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "account_id")
  private Long accountId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @Column(name = "seller_id")
  private String sellerId;

  @Column(name = "warehouse_id")
  private String warehouseId;

  @Column(name = "account_name", nullable = false, length = 255)
  private String accountName;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false, length = 30)
  private AccountStatus accountStatus;

  @Column(name = "email", length = 255, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "worker_code", length = 255, unique = true)
  private String workerCode;

  @Column(name = "phone_no", length = 255)
  private String phoneNo;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "password_changed_at")
  private LocalDateTime passwordChangedAt;

  @Column(name = "is_temporary_password", nullable = false)
  private Boolean isTemporaryPassword;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "updated_by", length = 255)
  private String updatedBy;

  protected Account() {
  }

  private Account(
      Role role,
      Tenant tenant,
      String sellerId,
      String warehouseId,
      String accountName,
      AccountStatus accountStatus,
      String email,
      String passwordHash,
      String workerCode,
      String phoneNo,
      Boolean isTemporaryPassword,
      String createdBy
  ) {
    this.role = role;
    this.tenant = tenant;
    this.sellerId = sellerId;
    this.warehouseId = warehouseId;
    this.accountName = accountName;
    this.accountStatus = accountStatus;
    this.email = email;
    this.passwordHash = passwordHash;
    this.workerCode = workerCode;
    this.phoneNo = phoneNo;
    this.isTemporaryPassword = isTemporaryPassword;
    this.createdBy = createdBy;
  }

  public static Account createEmailAccount(
      Role role,
      Tenant tenant,
      String sellerId,
      String warehouseId,
      String accountName,
      String email,
      String passwordHash,
      String phoneNo,
      AccountStatus accountStatus,
      Boolean isTemporaryPassword,
      String createdBy
  ) {
    return new Account(
        role,
        tenant,
        sellerId,
        warehouseId,
        accountName,
        accountStatus,
        email,
        passwordHash,
        null,
        phoneNo,
        isTemporaryPassword,
        createdBy
    );
  }

  public static Account createWorkerAccount(
      Role role,
      Tenant tenant,
      String warehouseId,
      String accountName,
      String workerCode,
      String passwordHash,
      String phoneNo,
      String createdBy
  ) {
    return new Account(
        role,
        tenant,
        null,
        warehouseId,
        accountName,
        AccountStatus.ACTIVE,
        null,
        passwordHash,
        workerCode,
        phoneNo,
        false,
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

  public void validateLoginAvailable() {
    if (this.accountStatus == AccountStatus.INVITED) {
      throw new IllegalStateException("아직 활성화되지 않은 계정입니다.");
    }
    if (this.accountStatus == AccountStatus.INACTIVE) {
      throw new IllegalStateException("비활성화된 계정입니다.");
    }
    if (this.accountStatus == AccountStatus.LOCKED) {
      throw new IllegalStateException("잠긴 계정입니다.");
    }
  }

  public void updateLastLoginAt(LocalDateTime lastLoginAt, String updatedBy) {
    this.lastLoginAt = lastLoginAt;
    this.updatedBy = updatedBy;
  }

  public void activate(String updatedBy) {
    this.accountStatus = AccountStatus.ACTIVE;
    this.updatedBy = updatedBy;
  }

  public void inactivate(String updatedBy) {
    this.accountStatus = AccountStatus.INACTIVE;
    this.updatedBy = updatedBy;
  }

  public void lock(String updatedBy) {
    this.accountStatus = AccountStatus.LOCKED;
    this.updatedBy = updatedBy;
  }

  public void changePassword(String newPasswordHash, Boolean isTemporaryPassword, String updatedBy) {
    this.passwordHash = newPasswordHash;
    this.isTemporaryPassword = isTemporaryPassword;
    this.passwordChangedAt = LocalDateTime.now();
    this.updatedBy = updatedBy;
  }

  public Long getAccountId() {
    return accountId;
  }

  public Role getRole() {
    return role;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public String getSellerId() {
    return sellerId;
  }

  public String getWarehouseId() {
    return warehouseId;
  }

  public String getAccountName() {
    return accountName;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getWorkerCode() {
    return workerCode;
  }

  public String getPhoneNo() {
    return phoneNo;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public LocalDateTime getPasswordChangedAt() {
    return passwordChangedAt;
  }

  public Boolean getIsTemporaryPassword() {
    return isTemporaryPassword;
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
}
