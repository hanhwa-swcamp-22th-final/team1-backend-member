package com.conk.member.command.domain.repository;

/* MemberToken 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.MemberToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberTokenRepository extends JpaRepository<MemberToken, String> {
    Optional<MemberToken> findByTokenHash(String tokenHash);
}
