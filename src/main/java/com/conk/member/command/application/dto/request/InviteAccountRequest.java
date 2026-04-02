package com.conk.member.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InviteAccountRequest {

  @NotBlank
  private String role;
  @NotBlank
  private Long organizationId;
  @NotBlank
  private String name;
  @NotBlank
  @Email
  private String email;

}
