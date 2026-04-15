package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyLogListRequest {
    private String companyId;
    private String actor;
    private String action;
    private LocalDateTime from;
    private LocalDateTime to;
}
