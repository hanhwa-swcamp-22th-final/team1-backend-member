package com.conk.member.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 비즈니스 코드 채번용 시퀀스 엔티티다.
 * prefix 별 마지막 사용 번호를 보관한다.
 */
@Entity
@Table(name = "code_sequence")
public class CodeSequence {

    @Id
    @Column(name = "code_type", nullable = false, length = 30)
    private String codeType;

    @Column(name = "last_seq", nullable = false)
    private int lastSeq;

    protected CodeSequence() {
    }

    public static CodeSequence of(String codeType) {
        CodeSequence sequence = new CodeSequence();
        sequence.codeType = codeType;
        sequence.lastSeq = 0;
        return sequence;
    }

    public int increment() {
        return ++lastSeq;
    }
}
