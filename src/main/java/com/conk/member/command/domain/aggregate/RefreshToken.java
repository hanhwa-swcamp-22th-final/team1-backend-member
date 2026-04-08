package com.conk.member.command.domain.aggregate;

/*
 * Refresh Token을 DB에 저장하는 엔티티다.
 * 레퍼런스(chap03)와 동일하게 PK = accountId 구조를 사용한다.
 * accountId당 1행만 유지 — 재발급 시 save()로 덮어쓴다(upsert).
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "refresh_token")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;
}
