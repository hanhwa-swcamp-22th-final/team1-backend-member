package com.conk.member.query.infrastructure.mapper;

/*
 * 사용자 목록 조회 전용 MyBatis 매퍼다.
 * 관리자용 사용자 목록 조회, 소속 사용자 목록 조회처럼 검색 조건이 많은 쿼리를 SQL로 분리했다.
 */

import com.conk.member.query.dto.condition.UserSearchCondition;
import com.conk.member.query.dto.mapper.UserListItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberUserQueryMapper {
    List<UserListItem> findUsers(UserSearchCondition condition);
}
