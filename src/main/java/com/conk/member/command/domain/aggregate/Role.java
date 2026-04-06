package com.conk.member.command.domain.aggregate;

/*
 * account가 참조하는 역할 마스터 엔티티다.
 */

import com.conk.member.command.domain.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role extends BaseAuditEntity {
    @Id
    @Column(name = "role_id")
    private String roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true)
    private RoleName roleName;

    @Column(name = "role_description")
    private String roleDescription;

    @Column(name = "is_active")
    private Integer isActive = 1;


    public static Role create(
            String roleId,
            String roleName,
            String roleDescription,
            String createdBy
    ) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName(RoleName.valueOf(roleName));
        role.setRoleDescription(roleDescription);
        role.setIsActive(1);
        role.setCreatedBy(createdBy);
        role.setUpdatedBy(createdBy);
        return role;
    }
}
