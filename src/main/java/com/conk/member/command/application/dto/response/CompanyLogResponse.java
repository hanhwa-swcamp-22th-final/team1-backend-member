package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyLogResponse {
    private String logId;
    private String companyId;   // FE logColumns 기준 필드명
    private String at;          // ISO 문자열 (FE: { key: 'at', label: '일시' })
    private String actor;       // 실제 요청자 accountId
    private String action;      // 변경 내용
}
