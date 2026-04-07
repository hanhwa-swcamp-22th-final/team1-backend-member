package com.conk.member.command.domain.aggregate;

/*
 * 권한 변경 이력을 남기기 위한 엔티티다.
 * 테스트 스키마 및 MyBatis 조회 구조와 일치시켰다: history_id, role_id, permission_id, action_type, changed_by, changed_at
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
    @Column(name = "history_id")
    private String historyId;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "permission_id", nullable = false)
    private String permissionId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
