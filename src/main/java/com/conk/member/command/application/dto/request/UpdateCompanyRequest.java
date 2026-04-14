package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCompanyRequest {
    private String representativeName;
    private String businessNo;
    private String phoneNo;
    private String email;
    private String address;
    private String status;
}
