package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.SellerWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerWarehouseRepository extends JpaRepository<SellerWarehouse, String> {
    List<SellerWarehouse> findBySellerIdOrderByWarehouseIdAsc(String sellerId);

    List<SellerWarehouse> findBySellerIdInOrderBySellerIdAscWarehouseIdAsc(List<String> sellerIds);

    @Query("select distinct sw.warehouseId from SellerWarehouse sw where sw.tenantId = :tenantId order by sw.warehouseId asc")
    List<String> findDistinctWarehouseIdsByTenantId(@Param("tenantId") String tenantId);
}
