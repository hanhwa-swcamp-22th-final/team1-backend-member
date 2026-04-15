package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAdminUserRequest {
    private String name;
    private String email;

    @JsonAlias("accountStatus")
    private String status;

    public String getStatus() {
        if (status == null) {
            return null;
        }
        return switch (status.trim().toUpperCase()) {
            case "INVITE_PENDING" -> "TEMP_PASSWORD";
            case "INVITE_EXPIRED", "LOCKED" -> "INACTIVE";
            default -> status.trim().toUpperCase();
        };
    }
}
