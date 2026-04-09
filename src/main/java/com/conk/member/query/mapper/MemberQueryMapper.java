package com.conk.member.query.mapper;

/*
 * 멤버 조회를 SQL 중심으로 검증할 때 사용할 수 있는 MyBatis 매퍼다.
 * JPA Repository 대신 명시적인 SQL을 사용해 조회 성능/결과를 검증하는 예시로 넣었다.
 */

import com.conk.member.query.dto.response.UserListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberQueryMapper {

    List<UserListResponse> findUsers(
            @Param("tenantId") String tenantId,
            @Param("roleName") String roleName,
            @Param("accountStatus") String accountStatus,
            @Param("keyword") String keyword
    );
}
