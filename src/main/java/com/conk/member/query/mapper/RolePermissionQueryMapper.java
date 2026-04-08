package com.conk.member.query.mapper;

/*
 * RBAC 조회 전용 MyBatis 매퍼다.
 * MEM-019(권한 매트릭스), MEM-021(이력) 대응.
 */

import com.conk.member.query.dto.PermissionHistoryRequest;
import com.conk.member.query.dto.RolePermissionHistoryItem;
import com.conk.member.query.dto.RolePermissionMatrixRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionQueryMapper {
  List<RolePermissionMatrixRow> findRolePermissions(@Param("roleId") String roleId);
  List<RolePermissionHistoryItem> findRolePermissionHistory(PermissionHistoryRequest request);
}
