package com.conk.member.query.mapper;

/*
 * 셀러 회사 목록 조회 전용 MyBatis 매퍼다.
 * tenant/status/keyword 조건으로 seller 목록을 조회할 때 사용한다.
 */

import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SellerQueryMapper {
    List<SellerListResponse> findSellers(SellerListRequest request);
}
