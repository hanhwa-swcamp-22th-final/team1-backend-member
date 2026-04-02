package com.conk.member.command.application.service;

public interface PasswordHasher {

  boolean matches(String rawPassword, String encodedPassword);

}
