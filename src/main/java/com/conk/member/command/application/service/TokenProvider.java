package com.conk.member.command.application.service;

import com.conk.member.command.domain.aggregate.Account;

public interface TokenProvider {
  String createAccessToken(Account account);
}