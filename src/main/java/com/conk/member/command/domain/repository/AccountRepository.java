package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.Account;

import java.util.Optional;

public interface AccountRepository {
  Account save(Account account);
  Optional<Account> findById(Long accountId);
  Optional<Account> findByEmail(String email);
  Optional<Account> findByWorkerCode(String workerCode);
  boolean existsByEmail(String email);
  boolean existsByWorkerCode(String workerCode);
  void delete(Account account);
}