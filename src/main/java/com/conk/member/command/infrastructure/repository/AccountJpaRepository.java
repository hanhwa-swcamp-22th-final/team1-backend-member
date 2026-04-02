package com.conk.member.command.infrastructure.repository;

import com.conk.member.command.domain.aggregate.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByWorkerCode(String workerCode);
    boolean existsByEmail(String email);
    boolean existsByWorkerCode(String workerCode);
}