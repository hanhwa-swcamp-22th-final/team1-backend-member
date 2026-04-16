package com.conk.member.command.domain.repository;

/* Seller 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, String> {
    boolean existsByCustomerCode(String customerCode);

    @Query("select s from Seller s where (:tenantId is null or s.tenantId = :tenantId) and (:status is null or s.status = :status) and (:keyword is null or s.brandNameKo like concat('%', :keyword, '%') or s.brandNameEn like concat('%', :keyword, '%') or s.representativeName like concat('%', :keyword, '%'))")
    List<Seller> search(@Param("tenantId") String tenantId, @Param("status") String status, @Param("keyword") String keyword);
}
