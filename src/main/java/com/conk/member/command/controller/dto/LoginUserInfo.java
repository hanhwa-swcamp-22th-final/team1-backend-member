package com.conk.member.command.controller.dto;

import lombok.Getter;

@Getter
public class LoginUserInfo {

    private final Long id;
    private final String email;
    private final String name;
    private final String role;
    private final String status;
    private final String organization;

    public LoginUserInfo(
            Long id,
            String email,
            String name,
            String role,
            String status,
            String organization
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.status = status;
        this.organization = organization;
    }
}