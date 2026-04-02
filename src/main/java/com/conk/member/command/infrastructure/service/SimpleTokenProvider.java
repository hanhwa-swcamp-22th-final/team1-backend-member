package com.conk.member.command.infrastructure.service;

import com.conk.member.command.application.service.TokenProvider;
import com.conk.member.command.domain.aggregate.Account;
import org.springframework.stereotype.Component;

@Component
public class SimpleTokenProvider implements TokenProvider {

  @Override
  public String createAccessToken(Account account) {
    return "access-token-" + account.getAccountId();
  }
}
