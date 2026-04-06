package com.conk.member.command.domain.repository;

/*
 * InvitationRepository 저장 테스트다.
 * 초대 발송 시 상태/발송일/만료일이 정상 저장되는지 확인한다.
 */

import com.conk.member.command.domain.aggregate.Invitation;
import com.conk.member.command.domain.enums.InviteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InvitationRepositoryTest {

    @Autowired
    private InvitationRepository invitationRepository;

    @Test
    @DisplayName("초대 정보를 저장하면 PENDING 상태와 발송 시각이 기록된다")
    void save_invitation() {
        Invitation invitation = new Invitation();
        invitation.setInvitationId("INV-001");
        invitation.setInviterAccountId("ACC-ADMIN");
        invitation.setInviteeAccountId("ACC-USER");
        invitation.setTargetRoleId("ROLE-SELLER");
        invitation.setTenantId("TENANT-001");
        invitation.setSellerId("SELLER-001");
        invitation.setInviteEmail("seller@conk.com");
        invitation.markPending();

        Invitation saved = invitationRepository.save(invitation);

        assertThat(saved.getInviteStatus()).isEqualTo(InviteStatus.PENDING);
        assertThat(saved.getInviteSentAt()).isNotNull();
        assertThat(saved.getInviteExpiredAt()).isAfter(saved.getInviteSentAt());
        assertThat(invitationRepository.findById("INV-001")).isPresent();
    }
}
