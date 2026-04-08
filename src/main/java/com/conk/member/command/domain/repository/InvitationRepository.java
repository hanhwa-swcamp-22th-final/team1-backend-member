package com.conk.member.command.domain.repository;

/* Invitation 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, String> {
}
