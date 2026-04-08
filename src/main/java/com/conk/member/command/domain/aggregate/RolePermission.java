package com.conk.member.command.domain.aggregate;

/*
 * 역할별 메뉴 권한(읽기/쓰기/삭제)을 저장하는 엔티티다.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role_permission")
public class RolePermission extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_permission_pk")
    private String rolePermissionPk;
    @Column(name = "role_id", nullable = false)
    private String roleId;
    @Column(name = "permission_id", nullable = false)
    private String permissionId;
    @Column(name = "is_enabled")
    private Integer isEnabled;
    @Column(name = "can_read")
    private Integer canRead;
    @Column(name = "can_write")
    private Integer canWrite;
    @Column(name = "can_delete")
    private Integer canDelete;
}
