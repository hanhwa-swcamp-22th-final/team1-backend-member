package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCompanyLogRequest {
    private String id;
    private String companyId;
    private LocalDateTime at;
    private String actor;
    private String action;
}
