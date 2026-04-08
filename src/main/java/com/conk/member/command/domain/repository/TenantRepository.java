package com.conk.member.command.domain.repository;

/* Tenant 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    @Query("select t from Tenant t where (:keyword is null or t.tenantName like concat('%', :keyword, '%') or t.tenantCode like concat('%', :keyword, '%')) and (:status is null or cast(t.status as string) = :status)")
    List<Tenant> search(@Param("keyword") String keyword, @Param("status") String status);
}
