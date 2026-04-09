package com.conk.member.query.service;

import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.UserListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.UserListResponse;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;

    public UserQueryService(MemberUserQueryMapper memberUserQueryMapper) {
        this.memberUserQueryMapper = memberUserQueryMapper;
    }

    public List<UserListResponse> getUsers(String tenantId, String role, String status,
                                           String sellerId, String warehouseId, String keyword) {
        UserListRequest request = new UserListRequest();
        request.setTenantId(tenantId);
        request.setRole(role);
        request.setAccountStatus(status);
        request.setSellerId(sellerId);
        request.setWarehouseId(warehouseId);
        request.setKeyword(keyword);

        List<UserListResponse> result = new ArrayList<>();
        for (UserListResponse item : memberUserQueryMapper.findUsers(request)) {
            result.add(copyUser(item));
        }
        return result;
    }

    public List<AdminUserListResponse> getAdminUsers(String companyId, String role, String status, String keyword) {
        AdminUserListRequest request = new AdminUserListRequest();
        request.setCompanyId(companyId);
        request.setRole(role);
        request.setStatus(status);
        request.setKeyword(keyword);

        List<AdminUserListResponse> result = new ArrayList<>();
        for (AdminUserListResponse item : memberUserQueryMapper.findAdminUsers(request)) {
            result.add(copyAdminUser(item));
        }
        return result;
    }

    private UserListResponse copyUser(UserListResponse item) {
        UserListResponse dto = new UserListResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setEmail(item.getEmail());
        dto.setRole(item.getRole());
        dto.setAccountStatus(item.getAccountStatus());
        dto.setTenantId(item.getTenantId());
        dto.setSellerId(item.getSellerId());
        dto.setWarehouseId(item.getWarehouseId());
        dto.setWorkerCode(item.getWorkerCode());
        dto.setLastLoginAt(item.getLastLoginAt());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    private AdminUserListResponse copyAdminUser(AdminUserListResponse item) {
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
