package com.conk.member.command.application.dto.response;

import lombok.Getter;

@Getter
public class LoginResponse {

  private final Long accountId;
  private final String name;
  private final String email;
  private final String workerCode;
  private final String role;
  private final String status;
  private final String organization;
  private final String accessToken;

  public LoginResponse(
      Long accountId,
      String name,
      String email,
      String workerCode,
      String role,
      String status,
      String organization,
      String accessToken
  ) {
    this.accountId = accountId;
    this.name = name;
    this.email = email;
    this.workerCode = workerCode;
    this.role = role;
    this.status = status;
    this.organization = organization;
    this.accessToken = accessToken;
  }
}