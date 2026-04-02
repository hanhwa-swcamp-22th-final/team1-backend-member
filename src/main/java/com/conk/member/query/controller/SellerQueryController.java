package com.conk.member.query.controller;

import com.conk.member.command.controller.dto.ApiResponse;
import com.conk.member.common.MemberApiMockDataFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member/sellers")
public class SellerQueryController {

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerStats() {
        return ResponseEntity.ok(ApiResponse.success("getSellerStats", MemberApiMockDataFactory.sellerStats()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSellerList() {
        return ResponseEntity.ok(ApiResponse.success("getSellerList", MemberApiMockDataFactory.sellerList()));
    }
}
