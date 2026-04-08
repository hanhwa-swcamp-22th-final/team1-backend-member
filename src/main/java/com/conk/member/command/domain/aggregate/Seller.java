package com.conk.member.command.domain.aggregate;

/*
 * 셀러 회사(화주사) 정보를 보관하는 엔티티다.
 * 이용 창고 목록은 외부 창고 서비스 MSA 참조 전제를 따른다.
 */

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seller")
public class Seller extends BaseAuditEntity {
    @Id
    @Column(name = "seller_id")
    private String sellerId;
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    @Column(name = "seller_info")
    private String sellerInfo;
    @Column(name = "brand_name_ko", nullable = false)
    private String brandNameKo;
    @Column(name = "brand_name_en")
    private String brandNameEn;
    @Column(name = "representative_name", nullable = false)
    private String representativeName;
    @Column(name = "business_no")
    private String businessNo;
    @Column(name = "phone_no", nullable = false)
    private String phoneNo;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "category_name")
    private String categoryName;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "customer_code", nullable = false, unique = true)
    private String customerCode;
}
