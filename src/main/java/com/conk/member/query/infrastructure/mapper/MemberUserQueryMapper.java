package com.conk.member.query.infrastructure.mapper;

/*
 * 사용자 목록 조회 전용 MyBatis 매퍼다.
 * MEM-006(소속 사용자), MEM-016(관리자용 사용자) 대응.
 */

import com.conk.member.query.dto.condition.AdminUserSearchCondition;
import com.conk.member.query.dto.condition.UserSearchCondition;
import com.conk.member.query.dto.mapper.AdminUserListItem;
import com.conk.member.query.dto.mapper.UserListItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberUserQueryMapper {
    List<UserListItem> findUsers(UserSearchCondition condition);
    List<AdminUserListItem> findAdminUsers(AdminUserSearchCondition condition);
}
