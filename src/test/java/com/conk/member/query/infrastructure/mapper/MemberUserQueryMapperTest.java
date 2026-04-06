package com.conk.member.query.infrastructure.mapper;

/*
 * 사용자 목록 조회 매퍼 테스트다.
 * tenant/role/status/keyword/warehouse 같은 검색 조건이 실제 SQL에서 의도한 대로 적용되는지 검증한다.
 */

import com.conk.member.query.dto.condition.UserSearchCondition;
import com.conk.member.query.dto.mapper.UserListItem;
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
class MemberUserQueryMapperTest {
    @Autowired
    private MemberUserQueryMapper memberUserQueryMapper;
    @Test
    @DisplayName("tenant/role/status/keyword 조건으로 사용자 목록을 조회할 수 있다")
    void find_users_by_conditions() {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setTenantId("TENANT-001");
        condition.setRoleName("WAREHOUSE_MANAGER");
        condition.setAccountStatus("ACTIVE");
        condition.setKeyword("창고");
        List<UserListItem> users = memberUserQueryMapper.findUsers(condition);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("manager@conk.com");
        assertThat(users.get(0).getRole()).isEqualTo("WAREHOUSE_MANAGER");
        assertThat(users.get(0).getWarehouseId()).isEqualTo("WH-001");
    }
    @Test
    @DisplayName("workerCode 검색으로 작업자 계정을 조회할 수 있다")
    void find_worker_by_worker_code_keyword() {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setTenantId("TENANT-001");
        condition.setKeyword("WORKER-001");
        List<UserListItem> users = memberUserQueryMapper.findUsers(condition);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("현장작업자1");
        assertThat(users.get(0).getWorkerCode()).isEqualTo("WORKER-001");
    }
    @Test
    @DisplayName("warehouseId 조건으로 창고별 사용자만 조회할 수 있다")
    void find_users_by_warehouse_id() {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setWarehouseId("WH-001");
        List<UserListItem> users = memberUserQueryMapper.findUsers(condition);
        assertThat(users).extracting(UserListItem::getWarehouseId).containsOnly("WH-001");
        assertThat(users).extracting(UserListItem::getRole).contains("WAREHOUSE_MANAGER", "WAREHOUSE_WORKER");
    }
}
