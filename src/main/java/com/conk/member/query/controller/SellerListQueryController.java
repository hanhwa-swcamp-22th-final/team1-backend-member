package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.SellerSummary;
import com.conk.member.query.dto.SellerListRequest;
import com.conk.member.query.service.SellerListQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SellerListQueryController {

    private final SellerListQueryService sellerListQueryService;

    public SellerListQueryController(SellerListQueryService sellerListQueryService) {
        this.sellerListQueryService = sellerListQueryService;
    }

    @GetMapping("/member/sellers")
    public ApiResponse<List<SellerSummary>> getSellerList(SellerListRequest request) {
        return ApiResponse.ok("seller list", sellerListQueryService.getSellerList(request));
    }
}
