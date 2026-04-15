package com.conk.member.command.application.service;

import com.conk.member.command.domain.aggregate.CodeSequence;
import com.conk.member.command.domain.repository.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운영 화면에 노출되는 비즈니스 코드 채번을 담당한다.
 */
@Service
public class BusinessCodeGenerator {

    private final CodeSequenceRepository codeSequenceRepository;

    public BusinessCodeGenerator(CodeSequenceRepository codeSequenceRepository) {
        this.codeSequenceRepository = codeSequenceRepository;
    }

    @Transactional
    public String nextTenantCode() {
        return nextCode("TEN", 3);
    }

    @Transactional
    public String nextCustomerCode() {
        return nextCode("CUST", 6);
    }

    @Transactional
    public String nextSellerId() {
        return nextCode("SELLER", 6);
    }

    private String nextCode(String prefix, int width) {
        CodeSequence sequence = codeSequenceRepository.findByCodeTypeForUpdate(prefix)
                .orElseGet(() -> codeSequenceRepository.save(CodeSequence.of(prefix)));

        int next = sequence.increment();
        return prefix + "-" + String.format("%0" + width + "d", next);
    }
}
