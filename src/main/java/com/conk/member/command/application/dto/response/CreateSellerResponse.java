package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateSellerResponse {
    private String id;
    private String customerCode;
    private String brandNameKo;
    private String status;
    private LocalDateTime createdAt;
}
