package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InviteAccountRequest {
    private String role;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String organizationId;  // FE가 전송하는 단일 조직 ID (역할별로 sellerId/warehouseId에 매핑됨)
    private String name;
    private String email;
    private String employeeNumber;  // WH_WORKER 사번 → Account.workerCode 에 저장
}
