package com.conk.member.common.security;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Permission;
import com.conk.member.command.domain.aggregate.RolePermission;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.PermissionRepository;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RolePermissionAuthorizationService {

    private final AccountRepository accountRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionAuthorizationService(AccountRepository accountRepository,
                                              RolePermissionRepository rolePermissionRepository,
                                              PermissionRepository permissionRepository) {
        this.accountRepository = accountRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public boolean isAllowed(String accountId, String requestPath, String method) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null || account.getRole() == null) {
            return false;
        }

        RoleName roleName = account.getRole().getRoleName();
        if (roleName == RoleName.SYSTEM_ADMIN || roleName == RoleName.MASTER_ADMIN) {
            return true;
        }
        if (roleName != RoleName.WAREHOUSE_MANAGER && roleName != RoleName.WAREHOUSE_WORKER) {
            return true;
        }

        String menuName = resolveMenuName(requestPath);
        if (menuName == null) {
            return true;
        }

        Permission permission = permissionRepository.findByMenuName(menuName).orElse(null);
        if (permission == null) {
            return true;
        }

        RolePermission rolePermission = rolePermissionRepository
                .findByRoleIdAndPermissionId(account.getRole().getRoleId(), permission.getPermissionId())
                .orElse(null);

        if (rolePermission == null || !isTruthy(rolePermission.getIsEnabled())) {
            return false;
        }

        return switch (method) {
            case "GET", "HEAD", "OPTIONS" -> isTruthy(rolePermission.getCanRead());
            case "POST", "PUT", "PATCH" -> isTruthy(rolePermission.getCanWrite());
            case "DELETE" -> isTruthy(rolePermission.getCanDelete());
            default -> true;
        };
    }

    private String resolveMenuName(String requestPath) {
        if (requestPath == null) {
            return null;
        }
        if (requestPath.startsWith("/member/users") || requestPath.startsWith("/member/auth/invite")) {
            return "users";
        }
        if (requestPath.startsWith("/member/sellers")) {
            return "sellers";
        }
        if (requestPath.startsWith("/member/roles")) {
            return "rbac";
        }
        return null;
    }

    private boolean isTruthy(Integer value) {
        return value != null && value == 1;
    }
}
