package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.RolePermissionMatrix;
import com.conk.member.query.dto.RolePermissionRow;
import com.conk.member.query.dto.RolePermissionMatrixRequest;
import com.conk.member.query.dto.RolePermissionMatrixRow;
import com.conk.member.query.mapper.RolePermissionQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RolePermissionMatrixQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionQueryMapper rolePermissionQueryMapper;

    public RolePermissionMatrixQueryService(RoleRepository roleRepository,
                                            RolePermissionQueryMapper rolePermissionQueryMapper) {
        this.roleRepository = roleRepository;
        this.rolePermissionQueryMapper = rolePermissionQueryMapper;
    }

    public RolePermissionMatrix getRolePermissions(RolePermissionMatrixRequest request) {
        Role role = getRole(request.getRoleId(), request.getRoleName());
        validateRbacScope(role);

        List<RolePermissionMatrixRow> rows = rolePermissionQueryMapper.findRolePermissions(role.getRoleId());

        RolePermissionMatrix matrix = new RolePermissionMatrix();
        matrix.setRoleId(role.getRoleId());
        matrix.setRoleName(role.getRoleName().name());

        List<RolePermissionRow> permissions = new ArrayList<>();
        for (RolePermissionMatrixRow row : rows) {
            RolePermissionRow permissionRow = new RolePermissionRow();
            permissionRow.setPermissionId(row.getPermissionId());
            permissionRow.setPermissionName(row.getPermissionName());
            permissionRow.setMenuName(row.getMenuName());
            permissionRow.setIsEnabled(row.getIsEnabled());
            permissionRow.setCanRead(row.getCanRead());
            permissionRow.setCanWrite(row.getCanWrite());
            permissionRow.setCanDelete(row.getCanDelete());
            permissionRow.setChangedAt(row.getChangedAt());
            permissions.add(permissionRow);
        }

        matrix.setPermissions(permissions);
        return matrix;
    }

    private Role getRole(String roleId, String roleName) {
        if (StringUtils.hasText(roleId)) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        }
        if (StringUtils.hasText(roleName)) {
            RoleName parsedRoleName = parseRoleName(roleName);
            return roleRepository.findByRoleName(parsedRoleName)
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        }
        throw new MemberException(ErrorCode.BAD_REQUEST, "roleId 또는 roleName 중 하나는 필수입니다.");
    }

    private void validateRbacScope(Role role) {
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER
                && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
    }

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.valueOf(roleName);
        } catch (IllegalArgumentException exception) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "유효하지 않은 역할명입니다: " + roleName);
        }
    }
}
