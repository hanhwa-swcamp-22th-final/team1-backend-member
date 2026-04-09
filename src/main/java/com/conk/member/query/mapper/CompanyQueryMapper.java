package com.conk.member.query.mapper;

/*
 * 업체 목록/단건 조회 전용 MyBatis 매퍼다.
 * MEM-012(목록), MEM-013(단건) 대응.
 */

import com.conk.member.query.dto.request.CompanyListRequest;
import com.conk.member.query.dto.response.CompanyDetailResponse;
import com.conk.member.query.dto.response.CompanyListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompanyQueryMapper {
    List<CompanyListResponse> findCompanies(CompanyListRequest request);
    Optional<CompanyDetailResponse> findCompanyById(@Param("id") String id);
}
