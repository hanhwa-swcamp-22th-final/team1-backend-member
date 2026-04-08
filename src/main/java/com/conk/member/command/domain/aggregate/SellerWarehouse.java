package com.conk.member.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seller_warehouse")
public class SellerWarehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seller_warehouse_id")
    private String sellerWarehouseId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "warehouse_id", nullable = false)
    private String warehouseId;
}
