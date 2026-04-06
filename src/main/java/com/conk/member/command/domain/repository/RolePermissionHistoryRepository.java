package com.conk.member.command.domain.repository;

/* RolePermissionHistory 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.RolePermissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionHistoryRepository extends JpaRepository<RolePermissionHistory, String> {
    List<RolePermissionHistory> findByRoleId(String roleId);
}
