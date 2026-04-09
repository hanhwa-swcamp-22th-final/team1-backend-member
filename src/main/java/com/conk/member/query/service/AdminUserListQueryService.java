package com.conk.member.query.service;

import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
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

    public List<AdminUserListResponse> getAdminUsers(AdminUserListRequest request) {
        List<AdminUserListResponse> result = new ArrayList<>();
        for (AdminUserListResponse item : memberUserQueryMapper.findAdminUsers(request)) {
            result.add(copy(item));
        }
        return result;
    }

    private AdminUserListResponse copy(AdminUserListResponse item) {
        AdminUserListResponse dto = new AdminUserListResponse();
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
