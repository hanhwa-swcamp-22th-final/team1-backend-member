package com.conk.member.query.infrastructure.mapper;

/*
 * 업체 목록 조회 매퍼 테스트다.
 * 업체명/테넌트코드 검색과 seller/account 집계 컬럼이 올바르게 조회되는지 검증한다.
 */

import com.conk.member.query.dto.condition.CompanySearchCondition;
import com.conk.member.query.dto.mapper.CompanyListItem;
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
class CompanyQueryMapperTest {
    @Autowired
    private CompanyQueryMapper companyQueryMapper;
    @Test
    @DisplayName("상태와 검색어로 업체 목록과 집계값을 조회할 수 있다")
    void find_companies_with_aggregates() {
        CompanySearchCondition condition = new CompanySearchCondition();
        condition.setStatus("ACTIVE");
        condition.setKeyword("FASTSHIP");
        List<CompanyListItem> companies = companyQueryMapper.findCompanies(condition);
        assertThat(companies).hasSize(1);
        CompanyListItem company = companies.get(0);
        assertThat(company.getTenantCode()).isEqualTo("TEN-FASTSHIP-001");
        assertThat(company.getSellerCount()).isEqualTo(2);
        assertThat(company.getUserCount()).isEqualTo(4);
        assertThat(company.getWarehouseCount()).isEqualTo(0);
    }
}
