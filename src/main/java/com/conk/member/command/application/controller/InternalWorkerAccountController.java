package com.conk.member.command.application.controller;

import com.conk.member.command.application.dto.request.InternalWorkerAccountCreateRequest;
import com.conk.member.command.application.dto.request.InternalWorkerAccountUpdateRequest;
import com.conk.member.command.application.service.InternalWorkerAccountService;
import com.conk.member.common.util.ApiResponse;
import com.conk.member.query.dto.response.InternalWorkerAccountResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS 내부 작업자 API 컨트롤러다.
 */
@RestController
@RequestMapping("/member/internal/workers")
public class InternalWorkerAccountController {

    private final InternalWorkerAccountService internalWorkerAccountService;

    public InternalWorkerAccountController(InternalWorkerAccountService internalWorkerAccountService) {
        this.internalWorkerAccountService = internalWorkerAccountService;
    }

    @GetMapping
    public ApiResponse<List<InternalWorkerAccountResponse>> getWorkers(@RequestParam String tenantId) {
        return ApiResponse.ok("worker list", internalWorkerAccountService.getWorkers(tenantId));
    }

    @GetMapping("/{workerId}")
    public ApiResponse<InternalWorkerAccountResponse> getWorker(@RequestParam String tenantId,
                                                                @PathVariable String workerId) {
        return ApiResponse.ok("worker detail", internalWorkerAccountService.getWorker(tenantId, workerId));
    }

    @PostMapping
    public ApiResponse<InternalWorkerAccountResponse> createWorker(@RequestParam String tenantId,
                                                                   @RequestBody InternalWorkerAccountCreateRequest request) {
        return ApiResponse.ok("worker created", internalWorkerAccountService.createWorker(tenantId, request));
    }

    @PutMapping("/{workerId}")
    public ApiResponse<InternalWorkerAccountResponse> updateWorker(@RequestParam String tenantId,
                                                                   @PathVariable String workerId,
                                                                   @RequestBody InternalWorkerAccountUpdateRequest request) {
        return ApiResponse.ok("worker updated", internalWorkerAccountService.updateWorker(tenantId, workerId, request));
    }
}
