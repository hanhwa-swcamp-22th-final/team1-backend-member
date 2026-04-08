package com.conk.member.query.service;

import com.conk.member.query.dto.AdminUserSummary;
import com.conk.member.query.dto.UserSummary;
import com.conk.member.query.dto.AdminUserListRequest;
import com.conk.member.query.dto.UserListRequest;
import com.conk.member.query.dto.AdminUserListItem;
import com.conk.member.query.dto.UserListItem;
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

    public List<UserSummary> getUsers(String tenantId, String role, String status,
                                      String sellerId, String warehouseId, String keyword) {
        UserListRequest request = new UserListRequest();
        request.setTenantId(tenantId);
        request.setRole(role);
        request.setAccountStatus(status);
        request.setSellerId(sellerId);
        request.setWarehouseId(warehouseId);
        request.setKeyword(keyword);

        List<UserSummary> result = new ArrayList<>();
        for (UserListItem item : memberUserQueryMapper.findUsers(request)) {
            result.add(toUserSummary(item));
        }
        return result;
    }

    public List<AdminUserSummary> getAdminUsers(String companyId, String role, String status, String keyword) {
        AdminUserListRequest request = new AdminUserListRequest();
        request.setCompanyId(companyId);
        request.setRole(role);
        request.setStatus(status);
        request.setKeyword(keyword);

        List<AdminUserSummary> result = new ArrayList<>();
        for (AdminUserListItem item : memberUserQueryMapper.findAdminUsers(request)) {
            result.add(toAdminUserSummary(item));
        }
        return result;
    }

    private UserSummary toUserSummary(UserListItem item) {
        UserSummary dto = new UserSummary();
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
