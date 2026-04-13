package com.conk.member.query.controller;

import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import com.conk.member.query.controller.RoleQueryController;
import com.conk.member.query.dto.response.PermissionHistoryResponse;
import com.conk.member.query.dto.response.RolePermissionMatrixResponse;
import com.conk.member.query.service.RoleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleQueryController.class)
class RoleQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RoleQueryService roleQueryService;

    // ─── GET /member/roles/permissions ───────────────────────────────────────

    @Test
    @DisplayName("권한 매트릭스 조회 성공 - 200 OK")
    @WithMockUser
    void getRolePermissions_success_returns200() throws Exception {
        RolePermissionMatrixResponse response = new RolePermissionMatrixResponse();
        response.setRoleId("ROLE-002");
        response.setRoleName("WAREHOUSE_MANAGER");
        response.setPermissions(List.of());

        given(roleQueryService.getRolePermissions(any())).willReturn(response);

        mockMvc.perform(get("/member/roles/permissions")
                        .param("roleId", "ROLE-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleId").value("ROLE-002"))
                .andExpect(jsonPath("$.data.roleName").value("WAREHOUSE_MANAGER"));
    }

    @Test
    @DisplayName("roleId/roleName 없이 조회 - 400 Bad Request")
    @WithMockUser
    void getRolePermissions_noParam_returns400() throws Exception {
        given(roleQueryService.getRolePermissions(any()))
                .willThrow(new MemberException(ErrorCode.BAD_REQUEST, "roleId 또는 roleName 중 하나는 필수입니다."));

        mockMvc.perform(get("/member/roles/permissions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("RBAC 범위 외 역할 조회 - 403 Forbidden")
    @WithMockUser
    void getRolePermissions_scopeRestricted_returns403() throws Exception {
        given(roleQueryService.getRolePermissions(any()))
                .willThrow(new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED));

        mockMvc.perform(get("/member/roles/permissions")
                        .param("roleId", "ROLE-001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── GET /member/roles/{roleId}/permission-history ────────────────────────

    @Test
    @DisplayName("권한 변경 이력 조회 성공 - 200 OK")
    @WithMockUser
    void getRolePermissionHistory_success_returns200() throws Exception {
        PermissionHistoryResponse item = new PermissionHistoryResponse();
        item.setHistoryId("HIST-001");
        item.setChangedBy("admin@example.com");

        given(roleQueryService.getRolePermissionHistory(any())).willReturn(List.of(item));

        mockMvc.perform(get("/member/roles/ROLE-002/permission-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].historyId").value("HIST-001"))
                .andExpect(jsonPath("$.data[0].changedBy").value("admin@example.com"));
    }

    @Test
    @DisplayName("존재하지 않는 역할 이력 조회 - 404 Not Found")
    @WithMockUser
    void getRolePermissionHistory_notFound_returns404() throws Exception {
        given(roleQueryService.getRolePermissionHistory(any()))
                .willThrow(new MemberException(ErrorCode.NOT_FOUND));

        mockMvc.perform(get("/member/roles/ROLE-999/permission-history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("RBAC 범위 외 역할 이력 조회 - 403 Forbidden")
    @WithMockUser
    void getRolePermissionHistory_scopeRestricted_returns403() throws Exception {
        given(roleQueryService.getRolePermissionHistory(any()))
                .willThrow(new MemberException(ErrorCode.ROLE_SCOPE_RESTRICTED));

        mockMvc.perform(get("/member/roles/ROLE-001/permission-history"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
