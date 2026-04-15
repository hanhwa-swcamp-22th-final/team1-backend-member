package com.conk.member.command.domain.repository;

import com.conk.member.command.domain.aggregate.CompanyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyLogRepository extends JpaRepository<CompanyLog, String> {
}
