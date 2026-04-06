package com.conk.member.command.domain.aggregate;

import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "account")
public class Account extends BaseAuditEntity {

  @Id
  @Column(name = "account_id")
  private String accountId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "tenant_id")
  private String tenantId;

  @Column(name = "seller_id")
  private String sellerId;

  @Column(name = "warehouse_id")
  private String warehouseId;

  @Column(name = "account_name", nullable = false)
  private String accountName;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false)
  private AccountStatus accountStatus;

  @Column(name = "email")
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "worker_code")
  private String workerCode;

  @Column(name = "phone_no")
  private String phoneNo;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "password_changed_at")
  private LocalDateTime passwordChangedAt;

  @Column(name = "is_temporary_password")
  private Boolean isTemporaryPassword;

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
      boolean isTemporaryPassword,
      String createdBy
  ) {
    Account account = new Account();
    account.setAccountId("ACC-" + UUID.randomUUID());
    account.setRole(role);
    account.setTenantId(tenant != null ? tenant.getTenantId() : null);
    account.setSellerId(sellerId);
    account.setWarehouseId(warehouseId);
    account.setAccountName(accountName);
    account.setEmail(email);
    account.setPasswordHash(passwordHash);
    account.setPhoneNo(phoneNo);
    account.setAccountStatus(accountStatus);
    account.setIsTemporaryPassword(isTemporaryPassword);
    account.setCreatedBy(createdBy);
    account.setUpdatedBy(createdBy);
    return account;
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
    Account account = new Account();
    account.setAccountId("ACC-" + UUID.randomUUID());
    account.setRole(role);
    account.setTenantId(tenant != null ? tenant.getTenantId() : null);
    account.setWarehouseId(warehouseId);
    account.setAccountName(accountName);
    account.setWorkerCode(workerCode);
    account.setPasswordHash(passwordHash);
    account.setPhoneNo(phoneNo);
    account.setAccountStatus(AccountStatus.ACTIVE);
    account.setIsTemporaryPassword(Boolean.FALSE);
    account.setCreatedBy(createdBy);
    account.setUpdatedBy(createdBy);
    return account;
  }

  public void successLogin() {
    this.lastLoginAt = LocalDateTime.now();
  }

  public void deactivate() {
    this.accountStatus = AccountStatus.INACTIVE;
  }

  public void reactivate() {
    this.accountStatus = AccountStatus.ACTIVE;
  }

  public void applyTemporaryPassword(String encoded) {
    this.passwordHash = encoded;
    this.accountStatus = AccountStatus.TEMP_PASSWORD;
    this.isTemporaryPassword = Boolean.TRUE;
  }

  public void changePassword(String encoded) {
    this.passwordHash = encoded;
    this.accountStatus = AccountStatus.ACTIVE;
    this.isTemporaryPassword = Boolean.FALSE;
    this.passwordChangedAt = LocalDateTime.now();
  }

  public boolean isRole(RoleName roleName) {
    return role != null && role.getRoleName() == roleName;
  }
}