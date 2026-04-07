package com.conk.member.command.domain.aggregate;

/*
 * 시스템에서 관리하는 메뉴/기능 권한 항목 마스터 엔티티다.
 * role_permission 에서 FK로 참조하며, RBAC 매트릭스 화면에 menu_name/permission_name 을 노출한다.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @Column(name = "permission_id")
    private String permissionId;

    @Column(name = "permission_name", nullable = false)
    private String permissionName;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "permission_description")
    private String permissionDescription;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
