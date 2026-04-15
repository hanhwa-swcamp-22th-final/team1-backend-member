package com.conk.member.query.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class CompanyListRequest {
    private String id;
    private String keyword;
    private String search;
    private String status;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";

    public String getKeyword() {
        return StringUtils.hasText(keyword) ? keyword : search;
    }
}
