package com.conk.member.query.service;

/*
 * 조회 전용 로직을 담당하는 서비스다.
 * mapper가 가져온 조회 결과를 API 응답 DTO로 바꿔준다.
 */

import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.dto.QueryResponses;
import com.conk.member.query.dto.condition.AdminUserSearchCondition;
import com.conk.member.query.dto.condition.CompanySearchCondition;
import com.conk.member.query.dto.condition.PermissionHistorySearchCondition;
import com.conk.member.query.dto.condition.SellerSearchCondition;
import com.conk.member.query.dto.condition.UserSearchCondition;
import com.conk.member.query.dto.mapper.AdminUserListItem;
import com.conk.member.query.dto.mapper.CompanyDetailItem;
import com.conk.member.query.dto.mapper.CompanyListItem;
import com.conk.member.query.dto.mapper.RolePermissionHistoryItem;
import com.conk.member.query.dto.mapper.RolePermissionMatrixRow;
import com.conk.member.query.dto.mapper.SellerListItem;
import com.conk.member.query.dto.mapper.UserListItem;
import com.conk.member.query.infrastructure.mapper.CompanyQueryMapper;
import com.conk.member.query.infrastructure.mapper.MemberUserQueryMapper;
import com.conk.member.query.infrastructure.mapper.RolePermissionQueryMapper;
import com.conk.member.query.infrastructure.mapper.SellerQueryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberUserQueryMapper memberUserQueryMapper;
    private final SellerQueryMapper sellerQueryMapper;
    private final CompanyQueryMapper companyQueryMapper;
    private final RolePermissionQueryMapper rolePermissionQueryMapper;
    private final RoleRepository roleRepository;

    public MemberQueryService(MemberUserQueryMapper memberUserQueryMapper,
                              SellerQueryMapper sellerQueryMapper,
                              CompanyQueryMapper companyQueryMapper,
                              RolePermissionQueryMapper rolePermissionQueryMapper,
                              RoleRepository roleRepository) {
        this.memberUserQueryMapper = memberUserQueryMapper;
        this.sellerQueryMapper = sellerQueryMapper;
        this.companyQueryMapper = companyQueryMapper;
        this.rolePermissionQueryMapper = rolePermissionQueryMapper;
        this.roleRepository = roleRepository;
    }

    public List<QueryResponses.SellerSummary> getSellerList(String tenantId, String status, String keyword) {
        SellerSearchCondition condition = new SellerSearchCondition();
        condition.setTenantId(tenantId);
        condition.setStatus(status);
        condition.setKeyword(keyword);

        List<QueryResponses.SellerSummary> result = new ArrayList<>();
        for (SellerListItem item : sellerQueryMapper.findSellers(condition)) {
            result.add(toSellerSummary(item));
        }
        return result;
    }

    public List<QueryResponses.UserSummary> getUsers(String tenantId,
                                                     String role,
                                                     String status,
                                                     String sellerId,
                                                     String warehouseId,
                                                     String keyword) {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setTenantId(tenantId);
        condition.setRoleName(role);
        condition.setAccountStatus(status);
        condition.setSellerId(sellerId);
        condition.setWarehouseId(warehouseId);
        condition.setKeyword(keyword);

        List<QueryResponses.UserSummary> result = new ArrayList<>();
        for (UserListItem item : memberUserQueryMapper.findUsers(condition)) {
            result.add(toUserSummary(item));
        }
        return result;
    }

    public List<QueryResponses.CompanySummary> getCompanies(String keyword, String status) {
        CompanySearchCondition condition = new CompanySearchCondition();
        condition.setKeyword(keyword);
        condition.setStatus(status);

        List<QueryResponses.CompanySummary> result = new ArrayList<>();
        for (CompanyListItem item : companyQueryMapper.findCompanies(condition)) {
            result.add(toCompanySummary(item));
        }
        return result;
    }

    public QueryResponses.CompanyDetail getCompanyById(String id) {
        CompanyDetailItem item = companyQueryMapper.findCompanyById(id)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));

        QueryResponses.CompanyDetail detail = new QueryResponses.CompanyDetail();
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
        return detail;
    }

    public List<QueryResponses.AdminUserSummary> getAdminUsers(String companyId,
                                                               String role,
                                                               String status,
                                                               String keyword) {
        AdminUserSearchCondition condition = new AdminUserSearchCondition();
        condition.setCompanyId(companyId);
        condition.setRoleName(role);
        condition.setAccountStatus(status);
        condition.setKeyword(keyword);

        List<QueryResponses.AdminUserSummary> result = new ArrayList<>();
        for (AdminUserListItem item : memberUserQueryMapper.findAdminUsers(condition)) {
            result.add(toAdminUserSummary(item));
        }
        return result;
    }

    public QueryResponses.RolePermissionMatrix getRolePermissions(String roleId, String roleName) {
        Role role = getRole(roleId, roleName);
        validateRbacScope(role);

        List<RolePermissionMatrixRow> rows = rolePermissionQueryMapper.findRolePermissions(role.getRoleId());

        QueryResponses.RolePermissionMatrix matrix = new QueryResponses.RolePermissionMatrix();
        matrix.setRoleId(role.getRoleId());
        matrix.setRoleName(role.getRoleName().name());

        List<QueryResponses.RolePermissionMatrix.PermissionRow> permissions = new ArrayList<>();
        for (RolePermissionMatrixRow row : rows) {
            QueryResponses.RolePermissionMatrix.PermissionRow permissionRow =
                    new QueryResponses.RolePermissionMatrix.PermissionRow();
            permissionRow.setPermissionId(row.getPermissionId());
            permissionRow.setPermissionName(row.getPermissionName());
            permissionRow.setIsEnabled(row.getIsEnabled());
            permissionRow.setCanRead(row.getCanRead());
            permissionRow.setCanWrite(row.getCanWrite());
            permissionRow.setCanDelete(row.getCanDelete());
            permissions.add(permissionRow);
        }

        matrix.setPermissions(permissions);
        return matrix;
    }

    public List<QueryResponses.PermissionHistory> getRolePermissionHistory(
            String roleId,
            String changedBy,
            LocalDateTime changedAtFrom,
            LocalDateTime changedAtTo) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        validateRbacScope(role);

        PermissionHistorySearchCondition condition = new PermissionHistorySearchCondition();
        condition.setRoleId(roleId);
        condition.setChangedBy(changedBy);
        condition.setChangedAtFrom(changedAtFrom);
        condition.setChangedAtTo(changedAtTo);

        List<QueryResponses.PermissionHistory> result = new ArrayList<>();
        for (RolePermissionHistoryItem item : rolePermissionQueryMapper.findRolePermissionHistory(condition)) {
            QueryResponses.PermissionHistory history = new QueryResponses.PermissionHistory();
            history.setHistoryId(item.getHistoryId());
            history.setRoleId(item.getRoleId());
            history.setRoleName(item.getRoleName());
            history.setPermissionId(item.getPermissionId());
            history.setPermissionName(item.getPermissionName());
            history.setActionType(item.getActionType());
            history.setChangedBy(item.getChangedBy());
            history.setChangedAt(item.getChangedAt());
            result.add(history);
        }
        return result;
    }

    private Role getRole(String roleId, String roleName) {
        if (StringUtils.hasText(roleId)) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        }

        if (StringUtils.hasText(roleName)) {
            RoleName parsedRoleName = parseRoleName(roleName);
            return roleRepository.findByRoleName(parsedRoleName)
                    .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND));
        }

        throw new MemberException(ErrorCode.BAD_REQUEST, "roleId 또는 roleName 중 하나는 필수입니다.");
    }

    private QueryResponses.SellerSummary toSellerSummary(SellerListItem item) {
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
        return dto;
    }

    private QueryResponses.UserSummary toUserSummary(UserListItem item) {
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
        return dto;
    }

    private QueryResponses.CompanySummary toCompanySummary(CompanyListItem item) {
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
        return dto;
    }

    private QueryResponses.AdminUserSummary toAdminUserSummary(AdminUserListItem item) {
        QueryResponses.AdminUserSummary dto = new QueryResponses.AdminUserSummary();
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

    private void validateRbacScope(Role role) {
        if (role.getRoleName() != RoleName.WAREHOUSE_MANAGER
                && role.getRoleName() != RoleName.WAREHOUSE_WORKER) {
            throw new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED);
        }
    }

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.valueOf(roleName);
        } catch (IllegalArgumentException exception) {
            throw new MemberException(ErrorCode.BAD_REQUEST, "유효하지 않은 역할명입니다: " + roleName);
        }
    }
}
