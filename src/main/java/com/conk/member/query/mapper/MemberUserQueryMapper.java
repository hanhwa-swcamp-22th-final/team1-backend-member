package com.conk.member.query.mapper;

/*
 * 사용자 목록 조회 전용 MyBatis 매퍼다.
 * MEM-006(소속 사용자), MEM-016(관리자용 사용자) 대응.
 */

import com.conk.member.query.dto.AdminUserListRequest;
import com.conk.member.query.dto.UserListRequest;
import com.conk.member.query.dto.AdminUserListItem;
import com.conk.member.query.dto.UserListItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberUserQueryMapper {
    List<UserListItem> findUsers(UserListRequest request);
    List<AdminUserListItem> findAdminUsers(AdminUserListRequest request);
}
