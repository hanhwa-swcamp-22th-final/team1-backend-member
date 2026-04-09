package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.PermissionHistoryRequest;
import com.conk.member.query.dto.response.PermissionHistoryResponse;
import com.conk.member.query.mapper.RolePermissionQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionHistoryQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionQueryMapper rolePermissionQueryMapper;

    public PermissionHistoryQueryService(RoleRepository roleRepository,
                                         RolePermissionQueryMapper rolePermissionQueryMapper) {
        this.roleRepository = roleRepository;
        this.rolePermissionQueryMapper = rolePermissionQueryMapper;
    }

    public List<PermissionHistoryResponse> getRolePermissionHistory(PermissionHistoryRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        validateRbacScope(role);

        List<PermissionHistoryResponse> result = new ArrayList<>();
        for (PermissionHistoryResponse item : rolePermissionQueryMapper.findRolePermissionHistory(request)) {
            result.add(copy(item));
        }
        return result;
    }

    private PermissionHistoryResponse copy(PermissionHistoryResponse item) {
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

    private void validateRbacScope(Role role) {
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER
                && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
    }
}
