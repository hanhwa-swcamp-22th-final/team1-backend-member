package com.conk.member.command.application.service;

import com.conk.member.command.application.dto.request.CreateSellerRequest;
import com.conk.member.command.application.dto.response.CreateSellerResponse;
import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.enums.SellerStatus;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.infrastructure.service.WarehouseService;
import com.conk.member.common.exception.ErrorCode;
import com.conk.member.common.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SellerCommandService {

    private final SellerRepository sellerRepository;
    private final WarehouseService warehouseService;

    public SellerCommandService(SellerRepository sellerRepository, WarehouseService warehouseService) {
        this.sellerRepository = sellerRepository;
        this.warehouseService = warehouseService;
    }

    public CreateSellerResponse createSeller(CreateSellerRequest request) {
        if (request.getWarehouseIds() != null) {
            for (String warehouseId : request.getWarehouseIds()) {
                if (!warehouseService.exists(warehouseId)) {
                    throw new MemberException(ErrorCode.INVALID_REFERENCE, "유효하지 않은 창고입니다.");
                }
            }
        }

        Seller seller = new Seller();
        seller.setSellerId(generateId("SELLER"));
        seller.setTenantId(request.getTenantId());
        seller.setSellerInfo(request.getSellerInfo());
        seller.setBrandNameKo(request.getBrandNameKo());
        seller.setBrandNameEn(request.getBrandNameEn());
        seller.setRepresentativeName(request.getRepresentativeName());
        seller.setBusinessNo(request.getBusinessNo());
        seller.setPhoneNo(request.getPhoneNo());
        seller.setEmail(request.getEmail());
        seller.setCategoryName(request.getCategoryName());
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setCustomerCode(generateCode("CUST"));
        sellerRepository.save(seller);

        CreateSellerResponse response = new CreateSellerResponse();
        response.setId(seller.getSellerId());
        response.setCustomerCode(seller.getCustomerCode());
        response.setBrandNameKo(seller.getBrandNameKo());
        response.setStatus(seller.getStatus().name());
        response.setCreatedAt(seller.getCreatedAt());
        return response;
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
