package com.conk.member.repository;

import com.conk.member.command.domain.aggregate.MemberToken;
import com.conk.member.command.domain.enums.TokenType;
import com.conk.member.command.domain.repository.MemberTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MemberTokenRepositoryTest {

    @Autowired MemberTokenRepository memberTokenRepository;

    private MemberToken buildToken(String tokenId, String tokenHash, boolean isUsed, LocalDateTime expiresAt) {
        MemberToken token = new MemberToken();
        token.setTokenId(tokenId);
        token.setAccountId("ACC-001");
        token.setTokenHash(tokenHash);
        token.setTokenType(TokenType.INITIAL_PASSWORD_SETUP);
        token.setExpiresAt(expiresAt);
        token.setIsUsed(isUsed);
        return token;
    }

    @Test
    @DisplayName("토큰 해시로 MemberToken 조회 성공")
    void findByTokenHash_success() {
        MemberToken token = buildToken("TOKEN-001", "abc123hash", false, LocalDateTime.now().plusDays(7));
        memberTokenRepository.save(token);

        Optional<MemberToken> result = memberTokenRepository.findByTokenHash("abc123hash");

        assertThat(result).isPresent();
        assertThat(result.get().getAccountId()).isEqualTo("ACC-001");
        assertThat(result.get().getTokenType()).isEqualTo(TokenType.INITIAL_PASSWORD_SETUP);
        assertThat(result.get().getIsUsed()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 해시로 조회 시 빈 Optional 반환")
    void findByTokenHash_notFound_returnsEmpty() {
        Optional<MemberToken> result = memberTokenRepository.findByTokenHash("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("만료된 토큰 isExpired() 반환 true")
    void isExpired_pastExpiresAt_returnsTrue() {
        MemberToken token = buildToken("TOKEN-002", "expiredhash", false, LocalDateTime.now().minusMinutes(1));
        memberTokenRepository.save(token);

        Optional<MemberToken> result = memberTokenRepository.findByTokenHash("expiredhash");

        assertThat(result).isPresent();
        assertThat(result.get().isExpired()).isTrue();
    }

    @Test
    @DisplayName("만료되지 않은 토큰 isExpired() 반환 false")
    void isExpired_futureExpiresAt_returnsFalse() {
        MemberToken token = buildToken("TOKEN-003", "validhash", false, LocalDateTime.now().plusDays(7));
        memberTokenRepository.save(token);

        Optional<MemberToken> result = memberTokenRepository.findByTokenHash("validhash");

        assertThat(result).isPresent();
        assertThat(result.get().isExpired()).isFalse();
    }

    @Test
    @DisplayName("use() 호출 후 isUsed가 true로 변경됨")
    void use_setsIsUsedToTrue() {
        MemberToken token = buildToken("TOKEN-004", "usehash", false, LocalDateTime.now().plusDays(7));
        memberTokenRepository.save(token);

        MemberToken saved = memberTokenRepository.findByTokenHash("usehash").get();
        saved.use();
        memberTokenRepository.save(saved);

        MemberToken updated = memberTokenRepository.findByTokenHash("usehash").get();
        assertThat(updated.getIsUsed()).isTrue();
    }
}
