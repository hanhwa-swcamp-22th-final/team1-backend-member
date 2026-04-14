package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCompanyLogRequest {
    private String companyId;   // FE 필드명 유지 (= tenantId)
    private String action;      // 변경 내용 설명
    // actor와 at은 BE에서 principal/서버 시각으로 자동 설정
}
