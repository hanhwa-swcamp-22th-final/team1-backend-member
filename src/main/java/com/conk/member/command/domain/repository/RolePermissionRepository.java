package com.conk.member.command.domain.repository;

/* RolePermission 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    List<RolePermission> findByRoleId(String roleId);
    Optional<RolePermission> findByRoleIdAndPermissionId(String roleId, String permissionId);
}
