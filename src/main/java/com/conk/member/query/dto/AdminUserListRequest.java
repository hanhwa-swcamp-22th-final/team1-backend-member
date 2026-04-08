package com.conk.member.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserListRequest {
    private String companyId;
    private String role;
    private String status;
    private String keyword;
}
