package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InviteAccountResponse {
    private String invitationId;
    private String role;
    private String tenantId;
    private String sellerId;
    private String warehouseId;
    private String name;
    private String email;
    private String workerCode;      // WH_WORKER 발급 시 설정, 나머지 역할은 null
    private String inviteStatus;
    private LocalDateTime inviteSentAt;
}
