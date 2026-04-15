package com.conk.member.query.controller;

import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.request.SellerFeeSummaryRequest;
import com.conk.member.query.dto.response.SellerFeeSummaryResponse;
import com.conk.member.query.service.SellerQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/member/seller", "/member/sellers"})
public class SellerFeeSummaryQueryController {

    private final SellerQueryService sellerQueryService;

    public SellerFeeSummaryQueryController(SellerQueryService sellerQueryService) {
        this.sellerQueryService = sellerQueryService;
    }

    @GetMapping("/fee-summary")
    public ResponseEntity<ApiResponse<List<SellerFeeSummaryResponse>>> getSellerFeeSummary(SellerFeeSummaryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("seller fee summary", sellerQueryService.getSellerFeeSummary(request)));
    }
}
