package com.conk.member.command.infrastructure.service;

import com.conk.member.command.application.service.PasswordHasher;
import org.springframework.stereotype.Component;

@Component
public class SimplePasswordHasher implements PasswordHasher {

   @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    if (rawPassword == null || encodedPassword == null) {
      return false;
    }
    return rawPassword.equals(encodedPassword);
  }
}
