package com.conk.member.query.mapper;

import com.conk.member.command.application.dto.response.CompanyLogResponse;
import com.conk.member.query.dto.request.CompanyLogListRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CompanyLogQueryMapper {
    List<CompanyLogResponse> findCompanyLogs(CompanyLogListRequest request);
}
