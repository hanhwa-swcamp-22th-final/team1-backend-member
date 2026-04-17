package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordResponse {
    private String accountId;
    private String accountStatus;
    private LocalDateTime passwordChangedAt;
}
