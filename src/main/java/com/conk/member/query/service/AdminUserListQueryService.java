package com.conk.member.query.service;

import com.conk.member.query.dto.AdminUserSummary;
import com.conk.member.query.dto.AdminUserListRequest;
import com.conk.member.query.dto.AdminUserListItem;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminUserListQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;

    public AdminUserListQueryService(MemberUserQueryMapper memberUserQueryMapper) {
        this.memberUserQueryMapper = memberUserQueryMapper;
    }

    public List<AdminUserSummary> getAdminUsers(AdminUserListRequest request) {
        List<AdminUserSummary> result = new ArrayList<>();
        for (AdminUserListItem item : memberUserQueryMapper.findAdminUsers(request)) {
            result.add(toAdminUserSummary(item));
        }
        return result;
    }

    private AdminUserSummary toAdminUserSummary(AdminUserListItem item) {
        AdminUserSummary dto = new AdminUserSummary();
        dto.setId(item.getId());
        dto.setCompanyId(item.getCompanyId());
        dto.setName(item.getName());
        dto.setEmail(item.getEmail());
        dto.setRole(item.getRole());
        dto.setOrganization(item.getOrganization());
        dto.setSellerId(item.getSellerId());
        dto.setWarehouseId(item.getWarehouseId());
        dto.setWorkerCode(item.getWorkerCode());
        dto.setStatus(item.getStatus());
        dto.setRegisteredAt(item.getRegisteredAt());
        dto.setLastLoginAt(item.getLastLoginAt());
        return dto;
    }
}
