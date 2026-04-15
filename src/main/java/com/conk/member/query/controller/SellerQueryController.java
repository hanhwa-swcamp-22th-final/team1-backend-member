package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.request.SellerRevenueRequest;
import com.conk.member.query.dto.request.SellerStatsRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerRevenueResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.service.SellerQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/member/sellers")
public class SellerQueryController {

    private final SellerQueryService sellerQueryService;

    public SellerQueryController(SellerQueryService sellerQueryService) {
        this.sellerQueryService = sellerQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerListResponse>>> getSellerList(SellerListRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("seller list", sellerQueryService.getSellerList(request)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SellerStatsResponse>> getSellerStats(SellerStatsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("seller stats", sellerQueryService.getSellerStats(request)));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<SellerRevenueResponse>>> getSellerRevenue(SellerRevenueRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("seller revenue", sellerQueryService.getSellerRevenue(request)));
    }

}
