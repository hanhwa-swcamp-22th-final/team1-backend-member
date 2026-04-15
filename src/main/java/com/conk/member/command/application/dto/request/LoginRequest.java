package com.conk.member.command.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
    @JsonAlias({"emailOrWorkerCode", "loginId"})
    private String email;

    private String password;

    public String getEmailOrWorkerCode() {
        return StringUtils.hasText(email) ? email : null;
    }
}
