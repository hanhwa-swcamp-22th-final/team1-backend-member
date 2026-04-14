package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.SellerListRequest;
import com.conk.member.query.dto.response.SellerListResponse;
import com.conk.member.query.dto.response.SellerStatsResponse;
import com.conk.member.query.service.SellerQueryService;
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
    public ApiResponse<List<SellerListResponse>> getSellerList(SellerListRequest request) {
        return ApiResponse.ok("seller list", sellerQueryService.getSellerList(request));
    }

    @GetMapping("/stats")
    public ApiResponse<SellerStatsResponse> getSellerStats() {
        return ApiResponse.ok("seller stats", sellerQueryService.getSellerStats());
    }
}
