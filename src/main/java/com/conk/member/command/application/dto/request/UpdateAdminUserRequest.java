package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAdminUserRequest {
    private String name;
    private String email;
    private String status;
}
