package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.TenantLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantLogRepository extends JpaRepository<TenantLog, String> {

    /**
     * 특정 업체의 최신 로그 최대 10건을 반환한다.
     * loggedAt 내림차순 정렬.
     */
    List<TenantLog> findTop10ByTenantIdOrderByLoggedAtDesc(String tenantId);
}
