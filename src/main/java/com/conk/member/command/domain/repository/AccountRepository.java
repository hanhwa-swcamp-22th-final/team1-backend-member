package com.conk.member.command.domain.repository;

/* Account 엔티티 저장소다. */

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByWorkerCode(String workerCode);
    boolean existsByEmail(String email);
    boolean existsByWorkerCode(String workerCode);

    @Query("select count(a) from Account a where a.tenantId = :tenantId and a.role.roleName = :roleName and a.accountStatus = :status")
    long countByTenantIdAndRoleNameAndAccountStatus(String tenantId, RoleName roleName, AccountStatus status);

    @Query("select a from Account a where (:tenantId is null or a.tenantId = :tenantId) and (:roleName is null or cast(a.role.roleName as string) = :roleName) and (:status is null or cast(a.accountStatus as string) = :status) and (:sellerId is null or a.sellerId = :sellerId) and (:warehouseId is null or a.warehouseId = :warehouseId) and (:keyword is null or a.accountName like concat('%', :keyword, '%') or a.email like concat('%', :keyword, '%') or a.workerCode like concat('%', :keyword, '%'))")
    List<Account> search(String tenantId, String roleName, String status, String sellerId, String warehouseId, String keyword);
}
