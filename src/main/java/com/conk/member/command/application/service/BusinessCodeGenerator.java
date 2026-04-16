package com.conk.member.command.application.service;

import com.conk.member.command.domain.aggregate.CodeSequence;
import com.conk.member.command.domain.repository.CodeSequenceRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

/**
 * 운영 화면에 노출되는 비즈니스 코드 채번을 담당한다.
 */
@Service
public class BusinessCodeGenerator {

    private final CodeSequenceRepository codeSequenceRepository;
    private final TenantRepository tenantRepository;
    private final SellerRepository sellerRepository;

    public BusinessCodeGenerator(CodeSequenceRepository codeSequenceRepository,
                                 TenantRepository tenantRepository,
                                 SellerRepository sellerRepository) {
        this.codeSequenceRepository = codeSequenceRepository;
        this.tenantRepository = tenantRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional
    public String nextTenantCode() {
        return nextUniqueCode("TEN", 3, tenantRepository::existsByTenantCode);
    }

    @Transactional
    public String nextCustomerCode() {
        return nextUniqueCode("CUST", 6, sellerRepository::existsByCustomerCode);
    }

    @Transactional
    public String nextSellerId() {
        return nextCode("SELLER", 6);
    }

    private String nextUniqueCode(String prefix, int width, Predicate<String> exists) {
        String candidate;
        do {
            candidate = nextCode(prefix, width);
        } while (exists.test(candidate));
        return candidate;
    }

    private String nextCode(String prefix, int width) {
        CodeSequence sequence = codeSequenceRepository.findByCodeTypeForUpdate(prefix)
                .orElseGet(() -> codeSequenceRepository.save(CodeSequence.of(prefix)));

        int next = sequence.increment();
        return prefix + "-" + String.format("%0" + width + "d", next);
    }
}
