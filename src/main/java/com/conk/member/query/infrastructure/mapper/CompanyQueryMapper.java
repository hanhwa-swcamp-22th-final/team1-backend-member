package com.conk.member.query.infrastructure.mapper;

/*
 * 업체 목록 조회 전용 MyBatis 매퍼다.
 * tenant 기본 정보와 seller/account 집계값을 함께 조회해 관리자 화면 목록에 사용한다.
 */

import com.conk.member.query.dto.condition.CompanySearchCondition;
import com.conk.member.query.dto.mapper.CompanyListItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CompanyQueryMapper {
    List<CompanyListItem> findCompanies(CompanySearchCondition condition);
}
