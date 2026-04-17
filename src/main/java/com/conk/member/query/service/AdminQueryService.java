package com.conk.member.query.service;

import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.SellerWarehouseRepository;
import com.conk.member.command.infrastructure.service.WmsWarehouseClient;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.CompanyDetailRequest;
import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyDetailResponse.WarehouseItem;
import com.conk.member.query.dto.response.CompanyListResponse;
import com.conk.member.query.dto.response.WmsWarehouseItem;
import com.conk.member.query.mapper.CompanyQueryMapper;
import com.conk.member.query.mapper.MemberUserQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AdminQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;
    private final CompanyQueryMapper companyQueryMapper;
    private final AccountRepository accountRepository;
    private final SellerWarehouseRepository sellerWarehouseRepository;
    private final SellerRepository sellerRepository;
    private final WmsWarehouseClient wmsWarehouseClient;

    public AdminQueryService(MemberUserQueryMapper memberUserQueryMapper,
                             CompanyQueryMapper companyQueryMapper,
                             AccountRepository accountRepository,
                             SellerWarehouseRepository sellerWarehouseRepository,
                             SellerRepository sellerRepository,
                             WmsWarehouseClient wmsWarehouseClient) {
        this.memberUserQueryMapper = memberUserQueryMapper;
        this.companyQueryMapper = companyQueryMapper;
        this.accountRepository = accountRepository;
        this.sellerWarehouseRepository = sellerWarehouseRepository;
        this.sellerRepository = sellerRepository;
        this.wmsWarehouseClient = wmsWarehouseClient;
    }

    // ─── getAdminUsers ────────────────────────────────────────────────────────

    public List<AdminUserListResponse> getAdminUsers(AdminUserListRequest request) {
        List<AdminUserListResponse> result = new ArrayList<>();
        for (AdminUserListResponse item : memberUserQueryMapper.findAdminUsers(request)) {
            result.add(copyAdminUser(item));
        }
        return result;
    }

    // ─── getCompanies ─────────────────────────────────────────────────────────

    public List<CompanyListResponse> getCompanies(CompanyListRequest request) {
        List<CompanyListResponse> result = new ArrayList<>();
        for (CompanyListResponse item : companyQueryMapper.findCompanies(request)) {
            result.add(toCompanyListResponse(item));
        }
        return result;
    }

    // ─── getCompanyById ───────────────────────────────────────────────────────

    public CompanyDetailResponse getCompanyById(CompanyDetailRequest request) {
        CompanyDetailResponse item = companyQueryMapper.findCompanyById(request.getId())
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        CompanyDetailResponse detail = new CompanyDetailResponse();
        detail.setId(item.getId());
        detail.setName(item.getName());
        detail.setTenantCode(item.getTenantCode());
        detail.setStatus(item.getStatus());
        detail.setCreatedAt(item.getCreatedAt());
        detail.setActivatedAt(item.getActivatedAt());
        detail.setRepresentative(item.getRepresentative());
        detail.setBusinessNumber(item.getBusinessNumber());
        detail.setPhone(item.getPhone());
        detail.setEmail(item.getEmail());
        detail.setAddress(item.getAddress());
        detail.setCompanyType(item.getCompanyType());
        detail.setSellerCount(item.getSellerCount());
        detail.setUserCount(item.getUserCount());

        // ① 셀러 회사명 목록 — member DB 내부 조회 (brandNameKo)
        List<String> sellerCompanyList = sellerRepository
                .search(detail.getId(), null, null)
                .stream()
                .map(Seller::getBrandNameKo)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        detail.setSellerCompanyList(sellerCompanyList);

        // ② 창고 목록 — wms-service 내부 HTTP 호출
        List<WarehouseItem> warehouseList = wmsWarehouseClient
                .findWarehousesByTenantId(detail.getId())
                .stream()
                .map(w -> new WarehouseItem(w.getCode(), w.getName(), w.getStatus()))
                .toList();
        detail.setWarehouseList(warehouseList);

        return detail;
    }

    // ─── private helpers ──────────────────────────────────────────────────────

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

    private CompanyListResponse toCompanyListResponse(CompanyListResponse item) {
        CompanyListResponse dto = new CompanyListResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setTenantCode(item.getTenantCode());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setActivatedAt(item.getActivatedAt());
        dto.setRepresentative(item.getRepresentative());
        dto.setBusinessNumber(item.getBusinessNumber());
        dto.setPhone(item.getPhone());
        dto.setEmail(item.getEmail());
        dto.setAddress(item.getAddress());
        dto.setCompanyType(item.getCompanyType());
        dto.setWarehouseCount(countWarehousesForTenant(item.getId()));
        dto.setSellerCount(item.getSellerCount());
        dto.setUserCount(item.getUserCount());
        return dto;
    }

    private int countWarehousesForTenant(String tenantId) {
        LinkedHashSet<String> warehouseIds = new LinkedHashSet<>();
        warehouseIds.addAll(accountRepository.findDistinctWarehouseIdsByTenantId(tenantId));
        warehouseIds.addAll(sellerWarehouseRepository.findDistinctWarehouseIdsByTenantId(tenantId));
        return warehouseIds.size();
    }
}
