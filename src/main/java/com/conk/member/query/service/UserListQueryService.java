package com.conk.member.query.service;

import com.conk.member.query.dto.UserSummary;
import com.conk.member.query.dto.UserListRequest;
import com.conk.member.query.dto.UserListItem;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserListQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;

    public UserListQueryService(MemberUserQueryMapper memberUserQueryMapper) {
        this.memberUserQueryMapper = memberUserQueryMapper;
    }

    public List<UserSummary> getUsers(UserListRequest request) {
        List<UserSummary> result = new ArrayList<>();
        for (UserListItem item : memberUserQueryMapper.findUsers(request)) {
            result.add(toUserSummary(item));
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
}
