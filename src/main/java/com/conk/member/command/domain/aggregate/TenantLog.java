package com.conk.member.command.domain.aggregate;

/*
 * 업체(Tenant) 관련 변경 이력을 기록하는 엔티티다.
 * 업체 등록, 정보 수정, 관리자 발급 등의 이벤트를 시간 순으로 보관한다.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenant_log")
public class TenantLog extends BaseAuditEntity {

    @Id
    @Column(name = "log_id")
    private String logId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "actor", nullable = false)
    private String actor;           // 실제 요청자 accountId (principal에서 추출)

    @Column(name = "action", nullable = false)
    private String action;          // 변경 내용 설명 (예: "업체 등록", "업체 기본 정보 수정")

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt; // 서버 시각 자동 설정
}
