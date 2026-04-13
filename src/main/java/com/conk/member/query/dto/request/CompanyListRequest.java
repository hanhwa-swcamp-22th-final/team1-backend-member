package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyListRequest {
    private String keyword;
    private String status;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
}
