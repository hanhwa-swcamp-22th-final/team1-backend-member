package com.conk.member.command.service;

import com.conk.member.command.application.service.BusinessCodeGenerator;
import com.conk.member.command.domain.aggregate.CodeSequence;
import com.conk.member.command.domain.repository.CodeSequenceRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BusinessCodeGeneratorTest {

    @Mock CodeSequenceRepository codeSequenceRepository;
    @Mock TenantRepository tenantRepository;
    @Mock SellerRepository sellerRepository;

    @InjectMocks BusinessCodeGenerator businessCodeGenerator;

    @Test
    @DisplayName("테넌트 코드 시퀀스가 실제 데이터보다 뒤처져 있어도 다음 유효 코드를 발급한다")
    void nextTenantCode_skipsExistingCodes() {
        CodeSequence sequence = CodeSequence.of("TEN");

        given(codeSequenceRepository.findByCodeTypeForUpdate("TEN")).willReturn(Optional.of(sequence));
        given(tenantRepository.existsByTenantCode("TEN-001")).willReturn(true);
        given(tenantRepository.existsByTenantCode("TEN-002")).willReturn(false);

        String code = businessCodeGenerator.nextTenantCode();

        assertThat(code).isEqualTo("TEN-002");
    }

    @Test
    @DisplayName("고객 코드 시퀀스가 실제 데이터보다 뒤처져 있어도 다음 유효 코드를 발급한다")
    void nextCustomerCode_skipsExistingCodes() {
        CodeSequence sequence = CodeSequence.of("CUST");

        given(codeSequenceRepository.findByCodeTypeForUpdate("CUST")).willReturn(Optional.of(sequence));
        given(sellerRepository.existsByCustomerCode("CUST-000001")).willReturn(true);
        given(sellerRepository.existsByCustomerCode("CUST-000002")).willReturn(false);

        String code = businessCodeGenerator.nextCustomerCode();

        assertThat(code).isEqualTo("CUST-000002");
    }
}
