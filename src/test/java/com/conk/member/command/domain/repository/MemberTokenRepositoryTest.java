package com.conk.member.command.domain.repository;

/*
 * MemberTokenRepository 토큰 해시 조회 테스트다.
 * 최초 비밀번호 설정 토큰을 해시값으로 찾는 흐름을 검증한다.
 */

import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.enums.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberTokenRepositoryTest {

    @Autowired
    private MemberTokenRepository memberTokenRepository;

    @Test
    @DisplayName("토큰 해시로 최초 비밀번호 설정 토큰을 조회할 수 있다")
    void find_by_token_hash() {
        MemberToken token = new MemberToken();
        token.setTokenId("TOKEN-001");
        token.setAccountId("ACC-001");
        token.setTokenHash("hashed-token");
        token.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setIsUsed(Boolean.FALSE);
        memberTokenRepository.save(token);

        assertThat(memberTokenRepository.findByTokenHash("hashed-token")).isPresent();
    }
}
