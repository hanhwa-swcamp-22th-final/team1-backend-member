package com.conk.member.query.service;

/*
 * 멤버 인증/인가 범위의 조회 전용 로직을 담당하는 서비스다.
 * 복잡한 목록 조회와 RBAC 조회는 MyBatis 매퍼를 사용하고, 역할 범위 같은 도메인 규칙만 보조적으로 검증한다.
 */

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.dto.condition.CompanySearchCondition;
import com.conk.member.query.dto.condition.SellerSearchCondition;
import com.conk.member.query.dto.condition.UserSearchCondition;
import com.conk.member.query.dto.mapper.CompanyListItem;
import com.conk.member.query.dto.mapper.RolePermissionHistoryItem;
import com.conk.member.query.dto.mapper.RolePermissionMatrixRow;
import com.conk.member.query.dto.mapper.SellerListItem;
import com.conk.member.query.dto.mapper.UserListItem;
import com.conk.member.query.infrastructure.mapper.CompanyQueryMapper;
import com.conk.member.query.infrastructure.mapper.MemberUserQueryMapper;
import com.conk.member.query.infrastructure.mapper.RolePermissionQueryMapper;
import com.conk.member.query.infrastructure.mapper.SellerQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;
    private final SellerQueryMapper sellerQueryMapper;
    private final CompanyQueryMapper companyQueryMapper;
    private final RolePermissionQueryMapper rolePermissionQueryMapper;
    private final RoleRepository roleRepository;

    public List<QueryResponses.SellerSummary> getSellerList(String tenantId, String status, String keyword) {
        SellerSearchCondition condition = new SellerSearchCondition();
        condition.setTenantId(tenantId);
        condition.setStatus(status);
        condition.setKeyword(keyword);
        List<QueryResponses.SellerSummary> result = new ArrayList<>();
        for (SellerListItem item : sellerQueryMapper.findSellers(condition)) {
            QueryResponses.SellerSummary dto = new QueryResponses.SellerSummary();
            dto.setId(item.getId());
            dto.setTenantId(item.getTenantId());
            dto.setCustomerCode(item.getCustomerCode());
            dto.setSellerInfo(item.getSellerInfo());
            dto.setBrandNameKo(item.getBrandNameKo());
            dto.setBrandNameEn(item.getBrandNameEn());
            dto.setRepresentativeName(item.getRepresentativeName());
            dto.setPhoneNo(item.getPhoneNo());
            dto.setEmail(item.getEmail());
            dto.setCategoryName(item.getCategoryName());
            dto.setStatus(item.getStatus());
            dto.setCreatedAt(item.getCreatedAt());
            result.add(dto);
        }
        return result;
    }

    public List<QueryResponses.UserSummary> getUsers(String tenantId, String role, String status, String sellerId, String warehouseId, String keyword) {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setTenantId(tenantId);
        condition.setRoleName(role);
        condition.setAccountStatus(status);
        condition.setSellerId(sellerId);
        condition.setWarehouseId(warehouseId);
        condition.setKeyword(keyword);
        List<QueryResponses.UserSummary> result = new ArrayList<>();
        for (UserListItem item : memberUserQueryMapper.findUsers(condition)) {
            QueryResponses.UserSummary dto = new QueryResponses.UserSummary();
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
            result.add(dto);
        }
        return result;
    }

    public List<QueryResponses.CompanySummary> getCompanies(String keyword, String status) {
        CompanySearchCondition condition = new CompanySearchCondition();
        condition.setKeyword(keyword);
        condition.setStatus(status);
        List<QueryResponses.CompanySummary> result = new ArrayList<>();
        for (CompanyListItem item : companyQueryMapper.findCompanies(condition)) {
            QueryResponses.CompanySummary dto = new QueryResponses.CompanySummary();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setTenantCode(item.getTenantCode());
            dto.setStatus(item.getStatus());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setRepresentative(item.getRepresentative());
            dto.setBusinessNumber(item.getBusinessNumber());
            dto.setPhone(item.getPhone());
            dto.setEmail(item.getEmail());
            dto.setAddress(item.getAddress());
            dto.setCompanyType(item.getCompanyType());
            dto.setWarehouseCount(item.getWarehouseCount());
            dto.setSellerCount(item.getSellerCount());
            dto.setUserCount(item.getUserCount());
            result.add(dto);
        }
        return result;
    }

    public QueryResponses.RolePermissionMatrix getRolePermissions(String roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        validateRbacScope(role);
        List<RolePermissionMatrixRow> rows = rolePermissionQueryMapper.findRolePermissions(roleId);
        QueryResponses.RolePermissionMatrix matrix = new QueryResponses.RolePermissionMatrix();
        matrix.setRoleId(role.getRoleId());
        matrix.setRoleName(role.getRoleName().name());
        List<QueryResponses.RolePermissionMatrix.PermissionRow> permissions = new ArrayList<>();
        for (RolePermissionMatrixRow row : rows) {
            QueryResponses.RolePermissionMatrix.PermissionRow permissionRow = new QueryResponses.RolePermissionMatrix.PermissionRow();
            permissionRow.setPermissionId(row.getPermissionId());
            permissionRow.setIsEnabled(row.getIsEnabled());
            permissionRow.setCanRead(row.getCanRead());
            permissionRow.setCanWrite(row.getCanWrite());
            permissionRow.setCanDelete(row.getCanDelete());
            permissions.add(permissionRow);
        }
        matrix.setPermissions(permissions);
        return matrix;
    }

    public List<QueryResponses.PermissionHistory> getRolePermissionHistory(String roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        validateRbacScope(role);
        List<QueryResponses.PermissionHistory> result = new ArrayList<>();
        for (RolePermissionHistoryItem item : rolePermissionQueryMapper.findRolePermissionHistory(roleId)) {
            QueryResponses.PermissionHistory dto = new QueryResponses.PermissionHistory();
            dto.setHistoryId(item.getHistoryId());
            dto.setRoleId(item.getRoleId());
            dto.setRoleName(item.getRoleName());
            dto.setPermissionId(item.getPermissionId());
            dto.setPermissionName(item.getPermissionName());
            dto.setActionType(item.getActionType());
            dto.setChangedBy(item.getChangedBy());
            dto.setChangedAt(item.getChangedAt());
            result.add(dto);
        }
        return result;
    }

    private void validateRbacScope(Role role) {
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
    }
}
