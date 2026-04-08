package com.conk.member.command.domain.aggregate;

/*
 * 이메일 기반 초대 정보를 보관하는 엔티티다.
 */

import com.conk.member.command.domain.enums.InviteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invitation")
public class Invitation extends BaseAuditEntity {
    @Id
    @Column(name = "invitation_id")
    private String invitationId;
    @Column(name = "inviter_account_id")
    private String inviterAccountId;
    @Column(name = "invitee_account_id")
    private String inviteeAccountId;
    @Column(name = "target_role_id", nullable = false)
    private String targetRoleId;
    @Column(name = "seller_id")
    private String sellerId;
    @Column(name = "tenant_id")
    private String tenantId;
    @Column(name = "warehouse_id")
    private String warehouseId;
    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false)
    private InviteStatus inviteStatus;
    @Column(name = "invite_sent_at")
    private LocalDateTime inviteSentAt;
    @Column(name = "invite_expired_at")
    private LocalDateTime inviteExpiredAt;
    @Column(name = "invite_email", nullable = false)
    private String inviteEmail;

    public void markPending() {
        this.inviteStatus = InviteStatus.PENDING;
        this.inviteSentAt = LocalDateTime.now();
        this.inviteExpiredAt = LocalDateTime.now().plusDays(7);
    }
}
