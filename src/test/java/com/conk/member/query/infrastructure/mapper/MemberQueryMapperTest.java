package com.conk.member.query.infrastructure.mapper;

/*
 * MyBatis 매퍼 테스트다.
 * user 목록 조회 SQL이 tenant/role/status/keyword 조건을 제대로 반영하는지 검증한다.
 */

import com.conk.member.query.dto.QueryResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ActiveProfiles("test")
@Sql(scripts = {
    "/sql/mapper/member-query-schema.sql",
    "/sql/mapper/member-query-data.sql"
})
class MemberQueryMapperTest {

    @Autowired
    private MemberQueryMapper memberQueryMapper;

    @Test
    @DisplayName("tenant/role/status/keyword 조건으로 사용자 목록을 조회할 수 있다")
    void find_users_by_conditions() {
        List<QueryResponses.UserSummary> users = memberQueryMapper.findUsers("TENANT-001", "WAREHOUSE_MANAGER", "ACTIVE", "창고");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("manager@conk.com");
        assertThat(users.get(0).getRole()).isEqualTo("WAREHOUSE_MANAGER");
        assertThat(users.get(0).getWarehouseId()).isEqualTo("WH-001");
    }
}
