package com.conk.member.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role")
public class Role {

  @Id
  @Column(name = "role_id", length = 255)
  private String roleId;

  @Column(name = "role_name", nullable = false, length = 255, unique = true)
  private String roleName;

  @Column(name = "role_description", length = 255)
  private String roleDescription;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @Column(name = "updated_by", length = 255)
  private String updatedBy;

  protected Role() {
  }

  private Role(
      String roleId,
      String roleName,
      String roleDescription,
      Boolean isActive,
      String createdBy
  ) {
    this.roleId = roleId;
    this.roleName = roleName;
    this.roleDescription = roleDescription;
    this.isActive = isActive;
    this.createdBy = createdBy;
  }

  public static Role create(
      String roleId,
      String roleName,
      String roleDescription,
      String createdBy
  ) {
    return new Role(
        roleId,
        roleName,
        roleDescription,
        true,
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

  public void deactivate(String updatedBy) {
    this.isActive = false;
    this.updatedBy = updatedBy;
  }

  public void activate(String updatedBy) {
    this.isActive = true;
    this.updatedBy = updatedBy;
  }

  public void changeDescription(String roleDescription, String updatedBy) {
    this.roleDescription = roleDescription;
    this.updatedBy = updatedBy;
  }

  public String getRoleId() {
    return roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getRoleDescription() {
    return roleDescription;
  }

  public Boolean getIsActive() {
    return isActive;
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