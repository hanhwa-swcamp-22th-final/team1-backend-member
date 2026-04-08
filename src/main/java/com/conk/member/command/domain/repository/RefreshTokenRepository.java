package com.conk.member.command.domain.repository;

/* RefreshToken 엔티티 저장소다. PK = accountId */

import com.conk.member.command.domain.aggregate.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}
