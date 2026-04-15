package com.conk.member.query.mapper;

import com.conk.member.query.dto.request.SellerFeeSummaryRequest;
import com.conk.member.query.dto.request.SellerRevenueRequest;
import com.conk.member.query.dto.request.SellerStatsRequest;
import com.conk.member.query.dto.response.SellerFeeSummaryResponse;
import com.conk.member.query.dto.response.SellerRevenueResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SellerMetricQueryMapper {
    List<SellerFeeSummaryResponse> findSellerFeeSummary(SellerFeeSummaryRequest request);
    List<SellerRevenueResponse> findSellerRevenue(SellerRevenueRequest request);
    SellerStatsResponse findSellerStats(SellerStatsRequest request);
}
