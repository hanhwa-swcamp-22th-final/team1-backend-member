package com.conk.member.command.domain.aggregate;

/*
 * 최초 비밀번호 설정/비밀번호 재설정용 토큰을 저장하는 엔티티다.
 * 원문 토큰은 저장하지 않고 해시값만 저장한다.
 */

import com.conk.member.command.domain.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "token")
public class MemberToken {
    @Id
    @Column(name = "token_id")
    private String tokenId;
    @Column(name = "account_id", nullable = false)
    private String accountId;
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "is_used")
    private Boolean isUsed;

    public boolean isExpired() { return expiresAt != null && expiresAt.isBefore(LocalDateTime.now()); }
    public void use() { this.isUsed = Boolean.TRUE; }
}
