package com.conk.member.query.infrastructure.mapper;

/*
 * RBAC 조회 전용 MyBatis 매퍼다.
 * 역할별 권한 매트릭스와 권한 변경 이력을 SQL로 조회해 관리자 화면과 테스트에서 재사용한다.
 */

import com.conk.member.query.dto.mapper.RolePermissionHistoryItem;
import com.conk.member.query.dto.mapper.RolePermissionMatrixRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionQueryMapper {

  List<RolePermissionMatrixRow> findRolePermissionMatrix(@Param("roleName") String roleName);

  List<RolePermissionHistoryItem> findRolePermissionHistory(@Param("roleId") String roleId);
}