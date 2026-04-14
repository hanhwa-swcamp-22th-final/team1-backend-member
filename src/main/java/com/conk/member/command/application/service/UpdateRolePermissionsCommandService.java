package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.PermissionUpdateRequest;
import com.conk.member.command.application.dto.request.UpdateRolePermissionsRequest;
import com.conk.member.command.application.dto.response.RolePermissionUpdateResponse;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.RolePermission;
import com.conk.member.command.domain.aggregate.RolePermissionHistory;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RolePermissionHistoryRepository;
import com.conk.member.command.domain.repository.RolePermissionRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UpdateRolePermissionsCommandService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RolePermissionHistoryRepository rolePermissionHistoryRepository;

    public UpdateRolePermissionsCommandService(RoleRepository roleRepository,
                                               RolePermissionRepository rolePermissionRepository,
                                               RolePermissionHistoryRepository rolePermissionHistoryRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.rolePermissionHistoryRepository = rolePermissionHistoryRepository;
    }

    public RolePermissionUpdateResponse updateRolePermissions(String roleId,
                                                              UpdateRolePermissionsRequest request,
                                                              String changedBy) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        if (!StringUtils.hasText(changedBy)) {
            throw new MemberException(ErrorCode.UNAUTHORIZED, "권한 변경 주체를 확인할 수 없습니다.");
        }

        validateRolePermissionScope(role);

        int updatedCount = 0;
        if (request.getPermissions() != null) {
            for (PermissionUpdateRequest permissionUpdate : request.getPermissions()) {
                updatedCount += updateSingleRolePermission(roleId, permissionUpdate, changedBy);
            }
        }

        RolePermissionUpdateResponse response = new RolePermissionUpdateResponse();
        response.setRoleId(roleId);
        response.setUpdatedPermissionCount(updatedCount);
        response.setChangedAt(LocalDateTime.now());
        response.setChangedBy(changedBy);
        return response;
    }

    private void validateRolePermissionScope(Role role) {
        if (!role.getRoleName().isWarehouseManager()
                && !role.getRoleName().isWarehouseWorker()) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
    }

    private int updateSingleRolePermission(String roleId, PermissionUpdateRequest permissionUpdate, String changedBy) {
        RolePermission rolePermission = rolePermissionRepository
                .findByRoleIdAndPermissionId(roleId, permissionUpdate.getPermissionId())
                .orElseGet(RolePermission::new);

        Integer beforeEnabled = rolePermission.getIsEnabled();
        Integer beforeRead = rolePermission.getCanRead();
        Integer beforeWrite = rolePermission.getCanWrite();
        Integer beforeDelete = rolePermission.getCanDelete();

        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionUpdate.getPermissionId());
        rolePermission.setIsEnabled(permissionUpdate.getIsEnabled());
        rolePermission.setCanRead(permissionUpdate.getCanRead());
        rolePermission.setCanWrite(permissionUpdate.getCanWrite());
        rolePermission.setCanDelete(permissionUpdate.getCanDelete());
        rolePermission.setUpdatedBy(changedBy);

        RolePermission savedRolePermission = rolePermissionRepository.save(rolePermission);
        boolean isNewPermission = beforeEnabled == null && beforeRead == null && beforeWrite == null && beforeDelete == null;

        RolePermissionHistory history = new RolePermissionHistory();
        history.setHistoryId(generateId("HIS"));
        history.setRoleId(roleId);
        history.setPermissionId(savedRolePermission.getPermissionId());
        history.setRolePermissionId(savedRolePermission.getRolePermissionPk());
        history.setActionType(isNewPermission ? "CREATE" : "UPDATE");
        history.setBeforeIsEnabled(beforeEnabled);
        history.setBeforeCanRead(beforeRead);
        history.setBeforeCanWrite(beforeWrite);
        history.setBeforeCanDelete(beforeDelete);
        history.setAfterCanRead(savedRolePermission.getCanRead());
        history.setAfterCanWrite(savedRolePermission.getCanWrite());
        history.setAfterCanDelete(savedRolePermission.getCanDelete());
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        rolePermissionHistoryRepository.save(history);

        return 1;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
