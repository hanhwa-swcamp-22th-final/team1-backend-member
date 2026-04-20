package com.conk.member.command.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserInfo {
        private String id;
        private String workerCode;
        private String name;
        private String email;
        private String role;
        private String status;
        private String organization;  // tenantName 매핑
        private String tenantId;
        private String sellerId;
        private String warehouseId;
    }
}
