package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByMenuName(String menuName);
}
