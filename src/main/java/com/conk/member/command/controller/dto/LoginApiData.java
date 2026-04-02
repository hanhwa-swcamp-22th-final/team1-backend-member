package com.conk.member.command.controller.dto;

import lombok.Getter;

@Getter
public class LoginApiData {

    private final String token;
    private final LoginUserInfo user;

    public LoginApiData(String token, LoginUserInfo user) {
        this.token = token;
        this.user = user;
    }
}