package com.conk.member.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InviteAccountResponse {
    private String role;
    private Long organizationId;
    private String name;
    private String email;
}