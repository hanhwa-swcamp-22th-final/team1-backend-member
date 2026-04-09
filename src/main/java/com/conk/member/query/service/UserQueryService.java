package com.conk.member.query.service;

import com.conk.member.query.dto.request.UserListRequest;
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

    public List<UserListResponse> getUsers(UserListRequest request) {
        List<UserListResponse> result = new ArrayList<>();
        for (UserListResponse item : memberUserQueryMapper.findUsers(request)) {
            result.add(copy(item));
        }
        return result;
    }

    private UserListResponse copy(UserListResponse item) {
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
}
