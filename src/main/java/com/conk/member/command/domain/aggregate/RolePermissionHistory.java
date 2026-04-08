package com.conk.member.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "history_id")
    private String historyId;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "permission_id", nullable = false)
    private String permissionId;

    @Column(name = "role_permission_id")
    private String rolePermissionId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "before_is_enabled")
    private Integer beforeIsEnabled;

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
