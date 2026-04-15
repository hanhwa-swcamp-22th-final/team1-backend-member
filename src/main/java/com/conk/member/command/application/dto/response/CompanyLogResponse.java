package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyLogResponse {
    private String id;
    private String companyId;
    private LocalDateTime at;
    private String actor;
    private String action;
}
