package com.conk.member.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * SecurityContext에 저장할 인증 사용자 정보다.
 * accountId뿐 아니라 필요한 사용자 식별값도 함께 보관한다.
 */
public class MemberUserPrincipal implements UserDetails {

    private final String accountId;
    private final String workerCode;
    private final String userName;
    private final String sellerId;
    private final String tenantId;
    private final String roleName;
    private final String passwordHash;
    private final List<? extends GrantedAuthority> authorities;

    public MemberUserPrincipal(String accountId,
                               String workerCode,
                               String userName,
                               String sellerId,
                               String tenantId,
                               String roleName,
                               String passwordHash,
                               List<? extends GrantedAuthority> authorities) {
        this.accountId = accountId;
        this.workerCode = workerCode;
        this.userName = userName;
        this.sellerId = sellerId;
        this.tenantId = tenantId;
        this.roleName = roleName;
        this.passwordHash = passwordHash;
        this.authorities = authorities;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getWorkerCode() {
        return workerCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return accountId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
