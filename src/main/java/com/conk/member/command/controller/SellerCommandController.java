package com.conk.member.command.controller;

import com.conk.member.command.controller.dto.ApiResponse;
import com.conk.member.common.MemberApiMockDataFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member/sellers")
public class SellerCommandController {

    @PostMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> registerSeller(@RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.ok(ApiResponse.success("registerSeller", MemberApiMockDataFactory.sellerList()));
    }
}
