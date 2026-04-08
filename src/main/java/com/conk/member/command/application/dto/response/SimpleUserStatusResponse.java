package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimpleUserStatusResponse {
    private String accountStatus;
    private Boolean isTemporaryPassword;
}
