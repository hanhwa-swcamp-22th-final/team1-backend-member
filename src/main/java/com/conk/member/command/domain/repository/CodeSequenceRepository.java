package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.CodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CodeSequenceRepository extends JpaRepository<CodeSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CodeSequence s WHERE s.codeType = :codeType")
    Optional<CodeSequence> findByCodeTypeForUpdate(String codeType);
}
