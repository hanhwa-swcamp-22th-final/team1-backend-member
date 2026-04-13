package com.conk.member.query.mapper;

/*
 * 사용자 목록 조회 전용 MyBatis 매퍼다.
 * MEM-006(소속 사용자), MEM-016(관리자용 사용자) 대응.
 */

import com.conk.member.query.dto.request.AdminUserListRequest;
import com.conk.member.query.dto.request.UserListRequest;
import com.conk.member.query.dto.response.AdminUserListResponse;
import com.conk.member.query.dto.response.UserListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberUserQueryMapper {
    List<UserListResponse> findUsers(UserListRequest request);
    List<AdminUserListResponse> findAdminUsers(AdminUserListRequest request);
}
