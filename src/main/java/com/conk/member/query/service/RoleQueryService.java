package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.PermissionHistoryRequest;
import com.conk.member.query.dto.request.RolePermissionMatrixRequest;
import com.conk.member.query.dto.response.PermissionHistoryResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixRowResponse;
import com.conk.member.query.dto.response.RolePermissionRowResponse;
import com.conk.member.query.mapper.RolePermissionQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionQueryMapper rolePermissionQueryMapper;

    public RoleQueryService(RoleRepository roleRepository,
                            RolePermissionQueryMapper rolePermissionQueryMapper) {
        this.roleRepository = roleRepository;
        this.rolePermissionQueryMapper = rolePermissionQueryMapper;
    }

    // ─── getRolePermissions ───────────────────────────────────────────────────

    public RolePermissionMatrixResponse getRolePermissions(RolePermissionMatrixRequest request) {
        Role role = getRole(request.getRoleId(), request.getRoleName());
        validateRbacScope(role);

        List<RolePermissionMatrixRowResponse> rows = rolePermissionQueryMapper.findRolePermissions(role.getRoleId());

        RolePermissionMatrixResponse matrix = new RolePermissionMatrixResponse();
        matrix.setRoleId(role.getRoleId());
        matrix.setRoleName(role.getRoleName().name());

        List<RolePermissionRowResponse> permissions = new ArrayList<>();
        for (RolePermissionMatrixRowResponse row : rows) {
            RolePermissionRowResponse permissionRow = new RolePermissionRowResponse();
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

    // ─── getRolePermissionHistory ─────────────────────────────────────────────

    public List<PermissionHistoryResponse> getRolePermissionHistory(PermissionHistoryRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        validateRbacScope(role);

        List<PermissionHistoryResponse> result = new ArrayList<>();
        for (PermissionHistoryResponse item : rolePermissionQueryMapper.findRolePermissionHistory(request)) {
            result.add(copyHistory(item));
        }
        return result;
    }

    // ─── private helpers ──────────────────────────────────────────────────────

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

    private PermissionHistoryResponse copyHistory(PermissionHistoryResponse item) {
        PermissionHistoryResponse history = new PermissionHistoryResponse();
        history.setHistoryId(item.getHistoryId());
        history.setRolePermissionId(item.getRolePermissionId());
        history.setBeforeIsEnabled(item.getBeforeIsEnabled());
        history.setBeforeCanRead(item.getBeforeCanRead());
        history.setBeforeCanWrite(item.getBeforeCanWrite());
        history.setBeforeCanDelete(item.getBeforeCanDelete());
        history.setAfterCanRead(item.getAfterCanRead());
        history.setAfterCanWrite(item.getAfterCanWrite());
        history.setAfterCanDelete(item.getAfterCanDelete());
        history.setChangedBy(item.getChangedBy());
        history.setChangedAt(item.getChangedAt());
        return history;
    }
}
