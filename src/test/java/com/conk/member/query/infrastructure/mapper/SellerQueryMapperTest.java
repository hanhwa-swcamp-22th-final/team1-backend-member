package com.conk.member.query.infrastructure.mapper;

/*
 * 셀러 회사 목록 조회 매퍼 테스트다.
 * tenant/status/keyword 조합으로 seller 검색이 제대로 되는지 확인한다.
 */

import com.conk.member.query.dto.condition.SellerSearchCondition;
import com.conk.member.query.dto.mapper.SellerListItem;
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
class SellerQueryMapperTest {
    @Autowired
    private SellerQueryMapper sellerQueryMapper;
    @Test
    @DisplayName("tenant와 상태 조건으로 셀러 회사 목록을 조회할 수 있다")
    void find_sellers_by_tenant_and_status() {
        SellerSearchCondition condition = new SellerSearchCondition();
        condition.setTenantId("TENANT-001");
        condition.setStatus("ACTIVE");
        List<SellerListItem> sellers = sellerQueryMapper.findSellers(condition);
        assertThat(sellers).hasSize(1);
        assertThat(sellers.get(0).getCustomerCode()).isEqualTo("CUST-001");
        assertThat(sellers.get(0).getBrandNameKo()).isEqualTo("한국미용상사");
    }
    @Test
    @DisplayName("검색어로 브랜드명과 대표자명을 조회할 수 있다")
    void find_sellers_by_keyword() {
        SellerSearchCondition condition = new SellerSearchCondition();
        condition.setTenantId("TENANT-001");
        condition.setKeyword("리빙");
        List<SellerListItem> sellers = sellerQueryMapper.findSellers(condition);
        assertThat(sellers).hasSize(1);
        assertThat(sellers.get(0).getBrandNameKo()).isEqualTo("리빙샵");
        assertThat(sellers.get(0).getRepresentativeName()).isEqualTo("박리빙");
    }
}
