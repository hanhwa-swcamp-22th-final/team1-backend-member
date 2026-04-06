package com.conk.member.command.domain.aggregate;

/*
 * 권한 변경 이력을 남기기 위한 엔티티다.
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
@Table(name = "role_permission_history")
public class RolePermissionHistory {
    @Id
    @Column(name = "permission_change_id")
    private String permissionChangeId;
    @Column(name = "role_permission_id", nullable = false)
    private String rolePermissionId;
    @Column(name = "role_id", nullable = false)
    private String roleId;
    @Column(name = "permission_id", nullable = false)
    private String permissionId;
    @Column(name = "before_can_read")
    private Integer beforeCanRead;
    @Column(name = "before_can_write")
    private Integer beforeCanWrite;
    @Column(name = "before_can_delete")
    private Integer beforeCanDelete;
    @Column(name = "after_can_read")
    private Integer afterCanRead;
    @Column(name = "after_can_write")
    private Integer afterCanWrite;
    @Column(name = "after_can_delete")
    private Integer afterCanDelete;
    @Column(name = "changed_by")
    private String changedBy;
    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
