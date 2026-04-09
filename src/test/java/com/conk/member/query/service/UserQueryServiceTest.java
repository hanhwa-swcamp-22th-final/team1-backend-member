package com.conk.member.query.service;

import com.conk.member.query.dto.request.UserListRequest;
import com.conk.member.query.dto.response.UserListResponse;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import com.conk.member.query.service.UserQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock MemberUserQueryMapper memberUserQueryMapper;

    @InjectMocks UserQueryService userQueryService;

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void getUsers_success() {
        UserListResponse item = new UserListResponse();
        item.setId("ACC-001");
        item.setName("홍길동");
        item.setRole("WAREHOUSE_WORKER");
        item.setAccountStatus("ACTIVE");
        item.setTenantId("TENANT-001");
        item.setWarehouseId("WH-001");
        item.setWorkerCode("WC-001");

        given(memberUserQueryMapper.findUsers(any())).willReturn(List.of(item));

        List<UserListResponse> result = userQueryService.getUsers(new UserListRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ACC-001");
        assertThat(result.get(0).getName()).isEqualTo("홍길동");
        assertThat(result.get(0).getRole()).isEqualTo("WAREHOUSE_WORKER");
        assertThat(result.get(0).getWorkerCode()).isEqualTo("WC-001");
    }

    @Test
    @DisplayName("사용자 목록 - 결과 없음")
    void getUsers_empty() {
        given(memberUserQueryMapper.findUsers(any())).willReturn(List.of());

        List<UserListResponse> result = userQueryService.getUsers(new UserListRequest());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("필터 조건으로 사용자 목록 조회")
    void getUsers_withFilter() {
        UserListRequest request = new UserListRequest();
        request.setTenantId("TENANT-001");
        request.setRole("WAREHOUSE_WORKER");
        request.setAccountStatus("ACTIVE");

        UserListResponse item = new UserListResponse();
        item.setId("ACC-001");
        item.setRole("WAREHOUSE_WORKER");
        item.setAccountStatus("ACTIVE");

        given(memberUserQueryMapper.findUsers(request)).willReturn(List.of(item));

        List<UserListResponse> result = userQueryService.getUsers(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountStatus()).isEqualTo("ACTIVE");
        then(memberUserQueryMapper).should().findUsers(request);
    }
}
