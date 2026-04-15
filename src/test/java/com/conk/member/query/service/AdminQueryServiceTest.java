package com.conk.member.query.service;

import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.CompanyDetailRequest;
import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.mapper.CompanyLogQueryMapper;
import com.conk.member.query.mapper.CompanyQueryMapper;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import com.conk.member.query.service.AdminQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminQueryServiceTest {

    @Mock MemberUserQueryMapper memberUserQueryMapper;
    @Mock CompanyQueryMapper companyQueryMapper;
    @Mock CompanyLogQueryMapper companyLogQueryMapper;
    @Mock AccountRepository accountRepository;
    @Mock SellerWarehouseRepository sellerWarehouseRepository;

    @InjectMocks AdminQueryService adminQueryService;

    // ─── getAdminUsers ────────────────────────────────────────────────────────

    @Test
    @DisplayName("관리자 사용자 목록 조회 성공")
    void getAdminUsers_success() {
        AdminUserListResponse item = new AdminUserListResponse();
        item.setId("ACC-001");
        item.setName("홍길동");
        item.setEmail("test@example.com");
        item.setRole("MASTER_ADMIN");
        item.setStatus("ACTIVE");

        given(memberUserQueryMapper.findAdminUsers(any())).willReturn(List.of(item));

        List<AdminUserListResponse> result = adminQueryService.getAdminUsers(new AdminUserListRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("ACC-001");
        assertThat(result.get(0).getName()).isEqualTo("홍길동");
        assertThat(result.get(0).getRole()).isEqualTo("MASTER_ADMIN");
    }

    @Test
    @DisplayName("관리자 사용자 목록 - 결과 없음")
    void getAdminUsers_empty() {
        given(memberUserQueryMapper.findAdminUsers(any())).willReturn(List.of());

        List<AdminUserListResponse> result = adminQueryService.getAdminUsers(new AdminUserListRequest());

        assertThat(result).isEmpty();
    }

    // ─── getCompanies ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("업체 목록 조회 성공")
    void getCompanies_success() {
        CompanyListResponse item = new CompanyListResponse();
        item.setId("TENANT-001");
        item.setName("테스트업체");
        item.setStatus("ACTIVE");

        given(companyQueryMapper.findCompanies(any())).willReturn(List.of(item));
        given(accountRepository.findDistinctWarehouseIdsByTenantId("TENANT-001")).willReturn(List.of("WH-001"));
        given(sellerWarehouseRepository.findDistinctWarehouseIdsByTenantId("TENANT-001")).willReturn(List.of());

        List<CompanyListResponse> result = adminQueryService.getCompanies(new CompanyListRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("TENANT-001");
        assertThat(result.get(0).getName()).isEqualTo("테스트업체");
        assertThat(result.get(0).getWarehouseCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("업체 목록 - 결과 없음")
    void getCompanies_empty() {
        given(companyQueryMapper.findCompanies(any())).willReturn(List.of());

        List<CompanyListResponse> result = adminQueryService.getCompanies(new CompanyListRequest());

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("업체 로그 목록 조회 성공")
    void getCompanyLogs_success() {
        com.conk.member.command.application.dto.response.CompanyLogResponse item = new com.conk.member.command.application.dto.response.CompanyLogResponse();
        item.setId("1712990000000");
        item.setCompanyId("TENANT-001");
        item.setActor("sys.admin@conk.com");
        item.setAction("총괄 관리자 추가 발급");

        com.conk.member.query.dto.request.CompanyLogListRequest request = new com.conk.member.query.dto.request.CompanyLogListRequest();
        request.setCompanyId("TENANT-001");

        given(companyLogQueryMapper.findCompanyLogs(request)).willReturn(List.of(item));

        List<com.conk.member.command.application.dto.response.CompanyLogResponse> result = adminQueryService.getCompanyLogs(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1712990000000");
        assertThat(result.get(0).getAction()).isEqualTo("총괄 관리자 추가 발급");
    }

    // ─── getCompanyById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("업체 단건 조회 성공")
    void getCompanyById_success() {
        CompanyDetailResponse item = new CompanyDetailResponse();
        item.setId("TENANT-001");
        item.setName("테스트업체");
        item.setStatus("ACTIVE");
        item.setSellerCount(3);
        item.setUserCount(10);

        CompanyDetailRequest request = new CompanyDetailRequest();
        request.setId("TENANT-001");

        given(companyQueryMapper.findCompanyById("TENANT-001")).willReturn(Optional.of(item));

        CompanyDetailResponse result = adminQueryService.getCompanyById(request);

        assertThat(result.getId()).isEqualTo("TENANT-001");
        assertThat(result.getName()).isEqualTo("테스트업체");
        assertThat(result.getSellerCount()).isEqualTo(3);
        assertThat(result.getUserCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 업체 조회 - 404 예외 발생")
    void getCompanyById_notFound_throwsException() {
        CompanyDetailRequest request = new CompanyDetailRequest();
        request.setId("TENANT-999");

        given(companyQueryMapper.findCompanyById("TENANT-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminQueryService.getCompanyById(request))
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }
}
