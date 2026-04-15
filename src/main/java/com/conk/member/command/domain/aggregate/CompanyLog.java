package com.conk.member.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "company_log")
public class CompanyLog {

    @Id
    @Column(name = "company_log_id")
    private String companyLogId;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "action_summary", nullable = false)
    private String actionSummary;

    @Column(name = "action_detail")
    private String actionDetail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
