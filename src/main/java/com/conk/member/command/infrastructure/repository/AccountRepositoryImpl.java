package com.conk.member.command.infrastructure.repository;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    public AccountRepositoryImpl(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    public Account save(Account account) {
        return accountJpaRepository.save(account);
    }
    @Override
    public Optional<Account> findById(Long accountId) {
        return accountJpaRepository.findById(accountId);
    }
    @Override
    public Optional<Account> findByEmail(String email) {
        return accountJpaRepository.findByEmail(email);
    }
    @Override
    public Optional<Account> findByWorkerCode(String workerCode) {
        return accountJpaRepository.findByWorkerCode(workerCode);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountJpaRepository.existsByEmail(email);
    }
    @Override
    public boolean existsByWorkerCode(String workerCode) {
        return accountJpaRepository.existsByWorkerCode(workerCode);
    }
    @Override
    public void delete(Account account) {
        accountJpaRepository.delete(account);
    }
}