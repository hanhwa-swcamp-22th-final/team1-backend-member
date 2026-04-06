package com.conk.member.query.infrastructure.mapper;

/*
 * 셀러 회사 목록 조회 전용 MyBatis 매퍼다.
 * tenant/status/keyword 조건으로 seller 목록을 조회할 때 사용한다.
 */

import com.conk.member.query.dto.condition.SellerSearchCondition;
import com.conk.member.query.dto.mapper.SellerListItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SellerQueryMapper {
    List<SellerListItem> findSellers(SellerSearchCondition condition);
}
