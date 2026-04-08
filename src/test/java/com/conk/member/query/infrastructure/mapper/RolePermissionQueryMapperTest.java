package com.conk.member.query.infrastructure.mapper;

/*
 * RBAC 조회 매퍼 테스트다.
 * 역할별 권한 매트릭스와 권한 변경 이력이 API 명세서에 필요한 형태로 조회되는지 확인한다.
 * RolePermissionHistoryCondition 변경에 맞게 테스트 수정.
 */

import com.conk.member.query.dto.condition.PermissionHistorySearchCondition;
import com.conk.member.query.dto.mapper.RolePermissionHistoryItem;
import com.conk.member.query.dto.mapper.RolePermissionMatrixRow;
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
class RolePermissionQueryMapperTest {

    @Autowired
    private RolePermissionQueryMapper rolePermissionQueryMapper;

    @Test
    @DisplayName("역할별 권한 매트릭스를 조회할 수 있다")
    void find_role_permissions() {
        List<RolePermissionMatrixRow> rows = rolePermissionQueryMapper.findRolePermissions("ROLE-002");
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(RolePermissionMatrixRow::getPermissionId)
                .containsExactlyInAnyOrder("PERM-001", "PERM-002");
        assertThat(rows).extracting(RolePermissionMatrixRow::getRoleName)
                .containsOnly("WAREHOUSE_MANAGER");
    }

    @Test
    @DisplayName("권한 변경 이력을 최신순으로 조회할 수 있다")
    void find_role_permission_history() {
        PermissionHistorySearchCondition condition = new PermissionHistorySearchCondition();
        condition.setRoleId("ROLE-002");

        List<RolePermissionHistoryItem> history =
                rolePermissionQueryMapper.findRolePermissionHistory(condition);

        assertThat(history).hasSize(2);
        // HIS-001(14:00) > HIS-002(13:50) 순으로 내림차순 정렬
        assertThat(history.get(0).getHistoryId()).isEqualTo("HIS-001");
        assertThat(history.get(0).getRolePermissionId()).isEqualTo("RP-001");
        assertThat(history.get(0).getBeforeCanWrite()).isEqualTo(0);
        assertThat(history.get(0).getAfterCanWrite()).isEqualTo(1);
        assertThat(history.get(1).getHistoryId()).isEqualTo("HIS-002");
    }

    @Test
    @DisplayName("changedBy 필터로 이력을 조회할 수 있다")
    void find_role_permission_history_filtered_by_changed_by() {
        PermissionHistorySearchCondition condition = new PermissionHistorySearchCondition();
        condition.setRoleId("ROLE-002");
        condition.setChangedBy("master@conk.com");

        List<RolePermissionHistoryItem> history =
                rolePermissionQueryMapper.findRolePermissionHistory(condition);

        assertThat(history).hasSize(2);
        assertThat(history).extracting(RolePermissionHistoryItem::getChangedBy)
                .containsOnly("master@conk.com");
    }
}
